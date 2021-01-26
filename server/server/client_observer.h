#ifndef CLIENT_OBSERVER_H
#define CLIENT_OBSERVER_H

#include <set>
#include <string.h>
#include <unordered_map>

class File;

class ClientHandler;

class ClientObserver {

public:
    ClientObserver(){};
    ~ClientObserver(){};

    void attachClient(ClientHandler*);
    void detachClient(ClientHandler*);
    void notify();
    void addFile(File*);
    std::unordered_map<std::string, File*>& getFilesMap() { return files_map_; };
    File* getFile(const std::string&);
    std::set<ClientHandler*>& getClientset() { return clients_set_; };

private:
    std::set<ClientHandler*> clients_set_;
    std::unordered_map<std::string, File*> files_map_;

};

#endif
