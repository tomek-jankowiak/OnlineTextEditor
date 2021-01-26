#ifndef FILE_H
#define FILE_H

#include <string.h>
#include <set>
#include <pthread.h>
#include <vector>

class ClientHandler;

class File {

public:
    File(const std::string& filename) 
        : filename_(filename) {
            buffer_length_ = 0;
            pthread_mutex_init(&this->buffer_update_mutex_, NULL); 
            };

    File(const File&) = delete;
    File& operator=(const File&) = delete;

    ~File();
    
    void attachUser(ClientHandler*);
    void detachUser(ClientHandler*);
    void updateBuffer(const char*, size_t);
    void notify(ClientHandler*);

    std::string getFilename() { return this->filename_; };
    std::string getBuffer() { return this->buffer_; };
    void setBuffer(const std::string& buffer) { this->buffer_ = buffer; };
    int getBufferLength() { return this->buffer_length_; };

private:
    std::string filename_;
    std::string buffer_;
    size_t buffer_length_;
    std::set<ClientHandler*> users_;

    pthread_mutex_t buffer_update_mutex_;
};

#endif
