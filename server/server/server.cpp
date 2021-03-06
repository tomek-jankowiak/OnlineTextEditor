#include "./server.h"

#include <arpa/inet.h>
#include <cstdio>
#include <netdb.h>
#include <netinet/in.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <unistd.h>

#include "./client_handler.h"
#include "./client_observer.h"
#include "../file/file.h"


void Server::Run() const {
    static int on = 1;

    sockaddr_in socket_addr;
    socket_addr.sin_family = AF_INET;
    socket_addr.sin_port = htons(port_);
    socket_addr.sin_addr.s_addr = INADDR_ANY;

    int server_socket_fd = socket(AF_INET, SOCK_STREAM, 0);
    setsockopt(server_socket_fd, SOL_SOCKET, SO_REUSEADDR, (char*)&on, sizeof(on));
    bind(server_socket_fd, (sockaddr*)&socket_addr, sizeof(socket_addr));
    listen(server_socket_fd, 5);
    std::printf("Server running at port %d\n", port_);
    socklen_t socklen = sizeof(sockaddr_in);

    std::unordered_map<std::string, File*> files_map;
    ClientObserver* client_observer = new ClientObserver();

    while(1) {
        ClientHandler* client_handler = new ClientHandler(client_observer);
        client_handler->socket_fd = accept(server_socket_fd, (sockaddr*)&client_handler->clientaddr, &socklen);
        client_observer->attachClient(client_handler);

        pthread_t thread_id;
        pthread_create(&thread_id, nullptr, HandleClient, client_handler);
        pthread_detach(thread_id);

        std::printf("New client connected from %s (socket fd: %d)\n", inet_ntoa((in_addr)client_handler->clientaddr.sin_addr), client_handler->socket_fd);
        std::printf("Clients connected: %lu\n", client_observer->getClientset().size());
    }

    delete client_observer;
    close(server_socket_fd);
}

void* Server::HandleClient(void* arg) {
    ClientHandler* client_handler = (ClientHandler*)arg;
    client_handler->Run();
    delete client_handler;
    return NULL;
}
