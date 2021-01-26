#ifndef CLIENT_HANDLER_H    
#define CLIENT_HANDLER_H

#include <netinet/in.h>
#include <unordered_set>
#include <unordered_map>

#include "../util/message_code.h"

struct ClientWriteStruct;

class ClientObserver;

class File;

class ClientHandler {

public:
    int socket_fd;
    sockaddr_in clientaddr;
    
    ClientHandler(ClientObserver* client_observer) 
        : client_observer_(client_observer), status_(client_connected){}

    void Run();

    void updateClient(ClientWriteStruct*);
    void setFile(File*);
    File* getFile() { return edited_file_; };
    ClientObserver* getClientObserver() { return client_observer_; }; 

private:
    ClientObserver* client_observer_;
    File* edited_file_;
    MessageCode status_;

    pthread_t writeThread_id;
    bool is_editing_;

    void ClientRead();

    static void* ClientWrite(void* arg);
};

#endif