#include "./client_handler.h"
#include "../file/file.h"
#include "../util/coding.h"

#include <cstdio>
#include <unistd.h>
#include <stdlib.h>

void ClientHandler::Run() {    
    if (this->edited_file->getBufferLength() > 0) {
        ClientStatus status = client_open_file;
        while (write(this->socket_fd, &status, 1) == 0) { continue; }

        size_t msg_size = this->edited_file->getBufferLength() + 4;
        char* buffer = new char[msg_size];
        EncodeFixed32(buffer, msg_size);
        buffer += 4;

        std::string file_buffer = this->edited_file->getBuffer();
        memcpy(buffer, file_buffer.c_str(), file_buffer.length());
        buffer -= 4;

        size_t write_count = 0;
        while (write_count != msg_size) {
            write_count += write(this->socket_fd, buffer, msg_size - write_count);
        }
    } else {
        ClientStatus status = client_new_file;
        while (write(this->socket_fd, &status, 1) == 0) { continue; }
    }

    while (this->status_ != client_close_connection) {
        char user_status[1];
        
        std::printf("Waiting for status\n");
        while (read(this->socket_fd, user_status, 1) == 0) { continue; }
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
    char length_buffer[4];
    read(this->socket_fd, length_buffer, 4);
    std::uint32_t msg_size = DecodeFixed32(length_buffer);

    char* client_buffer = new char[msg_size];

    size_t read_count = 0;
    while (read_count != msg_size) {
        read_count += read(this->socket_fd, client_buffer, msg_size - read_count);
    }

    this->edited_file->updateBuffer(client_buffer, msg_size);
    this->edited_file->notify(this);
}

void* ClientHandler::ClientWrite(void* arg) {
    ClientHandler* client_handler = (ClientHandler*)arg;

    size_t msg_size = client_handler->edited_file->getBufferLength() + 4;
    char* buffer = new char[msg_size];
    EncodeFixed32(buffer, msg_size);
    buffer += 4;

    std::string file_buffer = client_handler->edited_file->getBuffer();
    memcpy(buffer, file_buffer.c_str(), file_buffer.length());
    buffer -= 4;

    size_t write_count = 0;
    while (write_count != msg_size) {
        write_count += write(client_handler->socket_fd, buffer, msg_size - write_count);
    }
}