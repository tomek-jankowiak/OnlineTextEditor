#ifndef SERVER_H
#define SERVER_H

class Server {

public:
    Server(){};

    ~Server(){};

    void Run() const;

private:
    const int port_ = 1234;

    static void* HandleClient(void* arg);
};

#endif
