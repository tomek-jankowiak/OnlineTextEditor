#ifndef CLIENT_STATUS_H
#define CLIENT_STATUS_H 

enum MessageCode {
    client_connected = 1,
    client_create_new_file = 2,
    client_upload_new_file = 3,
    client_open_file = 4,
    client_update_file = 5,
    client_disconnected = 6
};

#endif
