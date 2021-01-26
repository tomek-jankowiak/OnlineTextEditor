#ifndef CLIENT_STATUS_H
#define CLIENT_STATUS_H 

enum MessageCode {
    client_connected = 10,
    client_create_new_file = 12,
    client_upload_new_file = 13,
    client_open_file = 14,
    client_update_file = 15,
    client_disconnected = 16,
    server_update_file_list = 20,
    server_update_client_file = 21 
};

#endif
