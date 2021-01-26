#include "./client_observer.h"

#include "./client_observer.h"
#include "../file/file.h"
#include "../util/client_write_struct.h"

void ClientObserver::attachClient(ClientHandler* client_handler) {
    clients_set_.emplace(client_handler);
    ClientWriteStruct* write_struct = new ClientWriteStruct();
    write_struct->message_code = server_update_file_list;
    write_struct->client_handler = client_handler;
    client_handler->updateClient(write_struct);
}

void ClientObserver::detachClient(ClientHandler* client_handler) {
    clients_set_.erase(client_handler);
}

void ClientObserver::notify() {
    for (const auto& client : clients_set_) {
        ClientWriteStruct* write_struct = new ClientWriteStruct();
        write_struct->message_code = server_update_file_list;
        write_struct->client_handler = client;
        client->updateClient(write_struct);
    }
}

void ClientObserver::addFile(File* file) {
    files_map_.emplace(file->getFilename(), file);
    notify();
}

File* ClientObserver::getFile(const std::string& filename) {
    return files_map_.at(filename);
}
