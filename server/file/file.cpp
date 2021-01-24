#include "./file.h"
#include "../server/client_handler.h"

File::~File() {
    pthread_mutex_destroy(&this->buffer_update_mutex_);
}

void File::attachUser(ClientHandler* client) {
    users_.insert(client);
    std::printf("Client inserted\n");
    if (this->buffer_length_ > 0) {
        client->updateFile();
    }
}

void File::detachUser(ClientHandler* client) {
    users_.erase(client);
}

void File::notify(ClientHandler* calling_client) {
    for (auto user : this->users_) {
        if (user == calling_client) {
            continue;
        }
        user->updateFile();
    }
}

void File::updateBuffer(const char* buffer, size_t size) {
    pthread_mutex_lock(&this->buffer_update_mutex_);
    this->buffer_ = std::string(buffer, size);
    this->buffer_length_ = size;
    pthread_mutex_unlock(&this->buffer_update_mutex_);
}