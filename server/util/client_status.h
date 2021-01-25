#ifndef CLIENT_STATUS_H
#define CLIENT_STATUS_H 

enum ClientStatus {
    client_new_connection = 1,
    client_new_file = 2,
    client_open_file = 3,
    client_update_file = 4,
    client_close_connection = 5
};

#endif
