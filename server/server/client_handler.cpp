#include "./client_handler.h"
#include "../file/file.h"
#include "../util/coding.h"

#include <cstdio>
#include <unistd.h>
#include <stdlib.h>


void ClientHandler::Run() {
    /*
        Send to new client a list of every file currently edited on the server.
        The format of the message is:
            * 4 bytes - encoded number of files
            Then, for every file:
            * 4 bytes - filename size
            * filename
    */
    size_t file_counter = this->files_map_.size();
    if (file_counter > 0) {
        size_t filenames_size = 0;
        for (const auto& file : this->files_map_) {
            filenames_size += file.first.length();
        }

        size_t msg_size = 4 * file_counter + filenames_size;
        char* buffer = new char[msg_size + 4];
        EncodeFixed32(buffer, file_counter);
        buffer += 4;

        for (const auto& file : this->files_map_) {
            size_t filename_size = file.first.length();
            EncodeFixed32(buffer, filename_size);
            buffer += 4;
            memcpy(buffer, file.first.c_str(), filename_size);
            buffer += filename_size;
        }
        buffer -= (msg_size + 4);

        size_t write_count = 0;
        while(write_count != msg_size + 4) {
            write_count += write(this->socket_fd, buffer, msg_size + 4 - write_count);
        }
    } else {
        char buffer[4];
        EncodeFixed32(buffer, 0);
        size_t write_count = 0;
        while(write_count !=  4) {
            write_count += write(this->socket_fd, buffer, 4 - write_count);
        }
    }

    while (this->status_ != client_disconnected) {
        this->ClientRead();
    }

    this->edited_file_->detachUser(this);

    std::printf("Client disconnected (socket_fd: %d).\n", this->socket_fd);
    close(this->socket_fd);
}

void ClientHandler::updateFile() {
    pthread_create(&this->writeThread_id, nullptr, ClientWrite, this);
    pthread_detach(this->writeThread_id);
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
            this->edited_file_ = new File(filename);
            this->edited_file_->attachUser(this);
            this->files_map_.emplace(filename, this->edited_file_);
            std::printf("Client created new file %s (socket_fd: %d)\n", this->edited_file_->getFilename().c_str(), this->socket_fd);
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
            this->edited_file_ = new File(file_params[0]);
            this->edited_file_->setBuffer(file_params[1]);
            this->edited_file_->attachUser(this);
            this->files_map_.emplace(file_params[0], this->edited_file_);
            std::printf("Client uploaded new file %s (socket_fd: %d)\n", this->edited_file_->getFilename().c_str(), this->socket_fd);
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
            this->edited_file_ = this->files_map_.at(filename);
            this->files_map_.at(filename)->attachUser(this);
            this->ClientWrite(this);
            std::printf("Client opened file %s (socket_fd: %d)\n", this->edited_file_->getFilename().c_str(), this->socket_fd);
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
    ClientHandler* client_handler = (ClientHandler*)arg;

    size_t msg_size = client_handler->edited_file_->getBufferLength();
    char* buffer = new char[msg_size + 4];
    EncodeFixed32(buffer, msg_size);
    buffer += 4;

    std::string file_buffer = client_handler->edited_file_->getBuffer();
    memcpy(buffer, file_buffer.c_str(), file_buffer.length());
    buffer -= 4;

    size_t write_count = 0;
    while (write_count != msg_size + 4) {
        write_count += write(client_handler->socket_fd, buffer, msg_size + 4 - write_count);
    }
}
