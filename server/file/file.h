#ifndef FILE_H
#define FILE_H

#include <string.h>
#include <set>
#include <unordered_set>
#include <pthread.h>

class ClientHandler;

class File {

public:
    File(const std::string& filename) 
        : filename_(filename) { 
            pthread_mutex_init(this->buffer_update_mutex_, NULL); 
            };

    File(const File&) = delete;
    File& operator=(const File&) = delete;

    ~File();
    
    void attachUser(ClientHandler*);
    void detachUser(ClientHandler*);
    void updateBuffer(const char*);
    void notify(ClientHandler*);

    std::string getFilename() { return this->filename_; };
    const char* getBuffer() { return this->buffer_; };

private:
    const std::string filename_;
    const char* buffer_;
    std::unordered_set<ClientHandler*> users_;

    pthread_mutex_t* buffer_update_mutex_;

};

#endif