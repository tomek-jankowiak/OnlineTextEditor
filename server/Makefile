CCX := g++
CCXFLAGS := -Wall -std=c++17 -pthread
LDFLAGS := -lpthread
EXEC := main.cpp
EXEC_OUTPUT := texteditor_server
OBJ := \
	file/file.o \
	server/client_handler.o \
	server/server.o
DEPS := \
	util/client_status.h

all: $(EXEC_OUTPUT)

$(EXEC_OUTPUT): $(EXEC) $(OBJ)
	$(CCX) $^ $(CCXFLAGS) $(LDFLAGS) -o $@

$(OBJ): %.o : %.cpp %.h $(DEPS)
	$(CCX) $< $(CCXFLAGS) -c -o $@

clean:
	rm -f $(OBJ)
