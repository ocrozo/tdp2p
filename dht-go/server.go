package main

import (
	"bufio"
	"fmt"
	"io"
	"net"
	"os"
)

func main() {
	// Change port corresponding to your team
	port := 1234
	srv, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		fmt.Println("Error creating server:", err)
		return
	}
	defer srv.Close()

	for {
		fmt.Println("Waiting for connections...")
		conn, err := srv.Accept()
		if err != nil {
			fmt.Println("Error accepting connection:", err)
			continue
		}

		go handleClient(conn)
	}
}

func handleClient(conn net.Conn) {
	defer conn.Close()

	fmt.Printf("Received connection from %s\n", conn.RemoteAddr())
	clientInput := bufio.NewReader(conn)
	clientOutput := conn

	level := 2

	// Read "level" information
	line, err := clientInput.ReadString('\n')
	if err == nil {
		fmt.Sscan(line, &level)
	}

	// Read the name of the requested file
	line, err = clientInput.ReadString('\n')
	if err == nil {
		fname := line[:len(line)-1]
		fmt.Printf("Client request for file %s...", fname)

		if fileInServer(fname, conn.RemoteAddr().String()) {
			f, err := os.Open("." + string(os.PathSeparator) + fname)
			if err == nil {
				copyStream(f, clientOutput)
				fmt.Println(" transfer done.")
				f.Close()
			} else {
				fmt.Println(" error:", err)
			}
		} else if level > 0 {
			fmt.Println(" file is not here, lookup further...")
			// Lookup on other known servers (decrement depth)
			found := lookupFurther(level-1, fname, clientOutput)
			fmt.Println(found)
		}
	}
}

func lookupFurther(level int, fname string, out io.Writer) bool {
	file, err := os.Open("servers.lst")
	if err != nil {
		fmt.Println("No servers.lst file, can't lookup further !")
		return false
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	found := false
	for scanner.Scan() {
		ip := scanner.Text()
		fmt.Printf("trying server %s\n", ip)
		s, err := net.Dial("tcp", fmt.Sprintf("%s:%d", ip, 1234))
		if err != nil {
			fmt.Println("Error while connecting to server:", err)
			continue
		}

		fmt.Fprintf(s, "%d\n%s\n", level, fname)
		nbytes := copyStream(s, out)
		s.Close()
		found = nbytes > 0
		if found {
			break
		}
	}

	if scanner.Err() != nil {
		fmt.Println("Error reading file:", scanner.Err())
	}

	return found
}

func copyStream(src io.Reader, dest io.Writer) int {
	nbytes, err := io.Copy(dest, src)
	if err != nil {
		fmt.Println("Error copying stream:", err)
	}
	return int(nbytes)
}

func fileInServer(fileName, ip string) bool {
	file, err := os.Open("files.lst")
	if err != nil {
		fmt.Println("No files.lst file, can't lookup files in this server !")
		return false
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	if scanner.Scan() {
		filePattern := scanner.Text()
		for _, c := range filePattern {
			if string(fileName[0]) == string(c) {
				file, err := os.Create("." + string(os.PathSeparator) + fileName)
				if err != nil {
					fmt.Println("Error creating file:", err)
					return false
				}
				defer file.Close()
				file.WriteString(ip + "\n")
				file.WriteString(ip + "\n")
				return true
			}
		}
	} else {
		fmt.Println("Error while reading pattern file !")
	}

	return false
}
