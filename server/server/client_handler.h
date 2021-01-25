#ifndef CLIENT_HANDLER_H    
#define CLIENT_HANDLER_H

#include <netinet/in.h>
#include <unordered_set>
#include <unordered_map>

#include "../util/message_code.h"

class File;

class ClientHandler {

public:
    int socket_fd;
    sockaddr_in clientaddr;
    
    ClientHandler(std::unordered_map<std::string, File*>& files_map) 
        : files_map_(files_map), status_(client_connected){}

    void Run();

    void updateFile();
    File* getFile() { return edited_file_; };

private:
    std::unordered_map<std::string, File*>& files_map_;
    File* edited_file_;
    MessageCode status_;

    pthread_t writeThread_id;

    void ClientRead();
    static void* ClientWrite(void* arg);
};

#endif