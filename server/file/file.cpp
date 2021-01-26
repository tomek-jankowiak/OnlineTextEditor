#include "./file.h"
#include "../server/client_handler.h"
#include "../util/client_write_struct.h"

File::~File() {
    pthread_mutex_destroy(&this->buffer_update_mutex_);
}

void File::attachUser(ClientHandler* client) {
    users_.emplace(client);
    if (this->buffer_length_ > 0) {
        ClientWriteStruct* write_struct = new ClientWriteStruct();
        write_struct->message_code = server_update_client_file;
        write_struct->client_handler = client;
        client->updateClient(write_struct);
    }
}

void File::detachUser(ClientHandler* client) {
    users_.erase(client);
}

void File::notify(ClientHandler* calling_client) {
    for (auto& user : this->users_) {
        if (user == calling_client) {
            continue;
        }
        ClientWriteStruct* write_struct = new ClientWriteStruct();
        write_struct->message_code = server_update_client_file;
        write_struct->client_handler = user;
        user->updateClient(write_struct);
    }
}

void File::updateBuffer(const char* buffer, size_t size) {
    pthread_mutex_lock(&this->buffer_update_mutex_);
    this->buffer_ = std::string(buffer, size);
    this->buffer_length_ = size;
    pthread_mutex_unlock(&this->buffer_update_mutex_);
}
