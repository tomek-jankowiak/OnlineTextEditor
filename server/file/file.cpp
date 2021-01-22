#include "./file.h"
#include "../server/client_handler.h"

File::~File() {
    delete[] this->buffer_;
    pthread_mutex_destroy(&this->buffer_update_mutex_);
}

void File::attachUser(ClientHandler* client) {
    users_.insert(client);
    client->updateFile();
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

void File::updateBuffer(const char* buffer) {
    pthread_mutex_lock(&this->buffer_update_mutex_);
    if (this->buffer_) {
        delete[] this->buffer_;
    }
    this->buffer_ = buffer;
    pthread_mutex_unlock(&this->buffer_update_mutex_);
}