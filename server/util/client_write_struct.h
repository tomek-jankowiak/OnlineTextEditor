#ifndef CLIENT_WRITE_STRUCT_H
#define CLIENT_WRITE_STRUCT_H

#include "./message_code.h"
#include "../server/client_handler.h"

class ClientHandler;

struct ClientWriteStruct {
    MessageCode message_code;
    ClientHandler* client_handler;
};

#endif