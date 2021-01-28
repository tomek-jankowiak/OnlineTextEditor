#include "./client_handler.h"

#include "./client_observer.h"
#include "../file/file.h"
#include "../util/coding.h"
#include "../util/client_write_struct.h"

#include <cstdio>
#include <unistd.h>
#include <stdlib.h>


void ClientHandler::Run() {

    is_editing_ = false;

    while (this->status_ != client_disconnected) {
        this->ClientRead();
    }

    if (is_editing_) {
        this->edited_file_->detachUser(this);
    }
    
    this->client_observer_->detachClient(this);
    
    std::printf("(socket_fd: %d) Client disconnected.\n", this->socket_fd);
    close(this->socket_fd);
}

void ClientHandler::updateClient(ClientWriteStruct* write_struct) {
    pthread_create(&this->writeThread_id, nullptr, ClientWrite, write_struct);
    pthread_detach(this->writeThread_id);
}

void ClientHandler::setFile(File* file) {
    edited_file_ = file;
}

void ClientHandler::ClientRead() {
    char user_status[1];
    while (read(this->socket_fd, user_status, 1) == 0) { continue; }
    this->status_ = (MessageCode)user_status[0];

    if (this->status_ == client_disconnected) { return; }
    
    switch (this->status_) {
        case client_create_new_file:
        /*  Received message format is:
                * 4 bytes - encoded filename size
                * filename buffer
        */
        {
            char length_buffer[4];
            read(this->socket_fd, length_buffer, 4);
            std::uint32_t msg_size = DecodeFixed32(length_buffer);

            char* new_file_name = new char[msg_size];
            size_t read_count = 0;
            while (read_count != msg_size) {
                read_count += read(this->socket_fd, new_file_name, msg_size - read_count);
            }

            std::string filename = std::string(new_file_name, msg_size);
            if (this->edited_file_) {
                this->edited_file_->detachUser(this);
            }
            this->edited_file_ = new File(filename);
            this->edited_file_->attachUser(this);
            this->client_observer_->addFile(this->edited_file_);
            std::printf("(socket_fd: %d) Client created new file %s\n", this->socket_fd, this->edited_file_->getFilename().c_str());
            is_editing_ = true;
            break;
        }
        case client_upload_new_file:
        /*  Received message format is:
                * 4 bytes - encoded filename size
                * filename buffer
                * 4 bytes - encoded buffer size
                * file buffer
        */
        {
            std::string file_params[2];
            for (int i = 0; i < 2; i++) {
                char length_buffer[4];
                read(this->socket_fd, length_buffer, 4);
                std::uint32_t msg_size = DecodeFixed32(length_buffer);

                char* buffer = new char[msg_size];
                size_t read_count = 0;
                while (read_count != msg_size) {
                    read_count += read(this->socket_fd, buffer, msg_size - read_count);
                }
                file_params[i] = std::string(buffer, msg_size);
            }
            if (this->edited_file_) {
                this->edited_file_->detachUser(this);
            }
            this->edited_file_ = new File(file_params[0]);
            this->edited_file_->setBuffer(file_params[1]);
            this->edited_file_->attachUser(this);
            this->client_observer_->addFile(this->edited_file_);
            std::printf("(socket_fd: %d) Client uploaded new file %s\n", this->socket_fd, this->edited_file_->getFilename().c_str());
            is_editing_ = true;
            break;
        }
        case client_open_file:
        /*  Received message format is:
                * 4 bytes - encoded filename size
                * filename buffer
        */
        {
            char length_buffer[4];
            read(this->socket_fd, length_buffer, 4);
            std::uint32_t msg_size = DecodeFixed32(length_buffer);

            char* buffer = new char[msg_size];

            size_t read_count = 0;
            while (read_count != msg_size) {
                read_count += read(this->socket_fd, buffer, msg_size - read_count);
            }

            std::string filename = std::string(buffer, msg_size);
            File* chosen_file = this->client_observer_->getFile(filename);
            if (this->edited_file_ != chosen_file) {
                if (this->edited_file_) {
                    this->edited_file_->detachUser(this);
                }
                this->edited_file_ = chosen_file;            
                this->edited_file_->attachUser(this);

                ClientWriteStruct* write_struct = new ClientWriteStruct();
                write_struct->message_code = server_update_client_file;
                write_struct->client_handler = this;
                this->ClientWrite(write_struct);
                std::printf("(socket_fd: %d) Client opened file %s\n", this->socket_fd, this->edited_file_->getFilename().c_str());
                is_editing_ = true;
            }
            break;
        }
        case client_update_file:
        /*  Received message format is:
                * 4 bytes - encoded buffer size
                * file buffer
        */
        {
            char length_buffer[4];
            read(this->socket_fd, length_buffer, 4);
            std::uint32_t msg_size = DecodeFixed32(length_buffer);

            char* buffer = new char[msg_size];

            size_t read_count = 0;
            while (read_count != msg_size) {
                read_count += read(this->socket_fd, buffer, msg_size - read_count);
            }

            this->edited_file_->updateBuffer(buffer, msg_size);
            this->edited_file_->notify(this);
            break;
        }
        default:
            break;
    }
}

void* ClientHandler::ClientWrite(void* arg) {
    ClientWriteStruct* write_struct = reinterpret_cast<ClientWriteStruct*>(arg);
    
    MessageCode message_code = write_struct->message_code;

    while (write(write_struct->client_handler->socket_fd, &message_code, 1) == 0) { continue; };

    if (message_code == server_update_client_file) {
        /*
            WRITE TO CLIENT AN UPDATED FILE BUFFER
            Message format is:
                * 4 bytes - file buffer size
                * file buffer   
        */
        size_t msg_size = write_struct->client_handler->getFile()->getBufferLength();
        char* buffer = new char[msg_size + 4];
        EncodeFixed32(buffer, msg_size);
        buffer += 4;

        std::string file_buffer = write_struct->client_handler->getFile()->getBuffer();
        memcpy(buffer, file_buffer.c_str(), file_buffer.length());
        buffer -= 4;

        size_t write_count = 0;
        while (write_count != msg_size + 4) {
            write_count += write(write_struct->client_handler->socket_fd, buffer, msg_size + 4 - write_count);
        }
    } else {
        /*
            WRITE TO CLIENT AN UPDATED FILE LIST
            Message format is:
                * 4 bytes - encoded number of files
                Then, for every file:
                * 4 bytes - filename size
                * filename
        */
       size_t file_counter = write_struct->client_handler->getClientObserver()->getFilesMap().size();
        if (file_counter > 0) {
            size_t filenames_size = 0;
            for (const auto& file : write_struct->client_handler->getClientObserver()->getFilesMap()) {
                filenames_size += file.first.length();
            }

            size_t msg_size = 4 * file_counter + filenames_size;
            char* buffer = new char[msg_size + 4];
            EncodeFixed32(buffer, file_counter);
            buffer += 4;

            for (const auto& file : write_struct->client_handler->getClientObserver()->getFilesMap()) {
                size_t filename_size = file.first.length();
                EncodeFixed32(buffer, filename_size);
                buffer += 4;
                memcpy(buffer, file.first.c_str(), filename_size);
                buffer += filename_size;
            }
            buffer -= (msg_size + 4);

            size_t write_count = 0;
            while(write_count != msg_size + 4) {
                write_count += write(write_struct->client_handler->socket_fd, buffer, msg_size + 4 - write_count);
            }
        } else {
            char buffer[4];
            EncodeFixed32(buffer, 0);
            size_t write_count = 0;
            while(write_count !=  4) {
                write_count += write(write_struct->client_handler->socket_fd, buffer, 4 - write_count);
            }
        }
    }

    delete write_struct;
    return NULL;
}
