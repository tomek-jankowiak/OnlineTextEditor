#ifndef CLIENT_HANDLER_H    
#define CLIENT_HANDLER_H

#include <netinet/in.h>
#include <unordered_set>

#include "../util/client_status.h"

class File;

class ClientHandler {

public:
    int socket_fd;
    sockaddr_in clientaddr;
    
    ClientHandler(File* file) 
        : edited_file(file), status_(client_new_connection){}

    void Run();

    void updateFile();

private:
    File* edited_file;
    ClientStatus status_;

    pthread_t writeThread_id;

    void ClientRead();
    static void* ClientWrite(void* arg);
};

#endif