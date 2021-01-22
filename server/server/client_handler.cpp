#include "./client_handler.h"
#include "../file/file.h"
#include "../util/coding.h"

#include <cstdio>
#include <unistd.h>
#include <stdlib.h>

void ClientHandler::Run() {
    //thread read
    //thread write

    // If there is no file opened, ask client to create one.
    /*if (!this->edited_file) {
        while(write(this->socket_fd, &client_new_file, 1) == 0) {
            continue
        }
        std::printf("New client status sent\n");

        char buffer[4];
        read(this->socket_fd, buffer, 4);
        std::printf("New file name size received, %s\n", buffer);

        size_t msg_size = std::atoi(buffer);
        char* new_filename = new char[msg_size];

        size_t read_count = 0;
        while (read_count != msg_size) {
            read_count += read(this->socket_fd, new_filename, msg_size - read_count);
        }
        std::printf("New file name received, %s\n", new_filename);

        char buffer2[4];
        read(this->socket_fd, buffer2, 4);
        std::printf("New file size received, %s", buffer2);

        msg_size = std::atoi(buffer2);
        char* new_file = new char[msg_size];
        
        read_count = 0;
        while (read_count != msg_size) {
            read_count += read(this->socket_fd, new_file, msg_size - read_count);
        }
        std::printf("File received, %s", new_file);

        this->edited_file = new File(std::string(new_filename));
        this->edited_file->updateBuffer(new_file);
    } else {
        const char* send_status = std::to_string(client_open_file).c_str();
        write(this->socket_fd, send_status, 1);
    }*/

    this->edited_file->attachUser(this);
    if (this->edited_file->getBuffer()) {
        while (write(this->socket_fd, &client_open_file, 1) == 0) { continue; }

        int buffer_size = strlen(this->edited_file->getBuffer()) + 4;
        char* buffer = new char[buffer_size];
        EncodeFixed32(buffer, buffer_size);
        buffer += 4;
        memcpy(buffer, this->edited_file->getBuffer(), buffer_size - 4);

        int write_count = 0;
        while (write_count != buffer_size) {
            write_count += write(this->socket_fd, buffer, buffer_size);
        }
    } else {
        while (write(this->socket_fd, &client_new_file, 1) == 0) { continue; }
    }

    while (this->status_ != client_close_connection) {
        char user_status[1];
        read(this->socket_fd, user_status, 1);
        this->status_ = (ClientStatus)std::atoi(user_status); 

        if (this->status_ == client_update_file) {
            this->ClientRead();
        }
    }

    this->edited_file->detachUser(this);
    close(this->socket_fd);
}

void ClientHandler::updateFile() {
    pthread_create(&this->writeThread_id, nullptr, ClientWrite, this);
    pthread_detach(this->writeThread_id);
}

void ClientHandler::ClientRead() {
    char buffer[4];
    read(this->socket_fd, buffer, 4);

    size_t msg_size = std::atoi(buffer);
    char* client_buffer = new char[msg_size];

    size_t read_count = 0;
    while (read_count != msg_size) {
        read_count += read(this->socket_fd, client_buffer, msg_size - read_count);
    }

    this->edited_file->updateBuffer(client_buffer);
    this->edited_file->notify(this);
}

void* ClientHandler::ClientWrite(void* arg) {
    ClientHandler* client_handler = (ClientHandler*)arg;

    size_t msg_size = strlen(client_handler->edited_file->getBuffer());
    const char* size_buffer = new char[4];
    size_buffer = std::to_string(msg_size).c_str();
    write(client_handler->socket_fd, size_buffer, 4);

    const char* msg_buffer = new char[msg_size];
    msg_buffer = client_handler->edited_file->getBuffer();

    size_t write_count = 0;
    while (write_count != msg_size) {
        write_count += write(client_handler->socket_fd, msg_buffer, msg_size - write_count);
    }
}