package main

import (
	"bufio"
	"fmt"
	"io"
	"net"
	"os"
)

func main() {
	ip := ""
	// Change port corresponding to your team
	port := 1234
	found := false

	// Open and read the "servers.lst" file
	file, err := os.Open("servers.lst")
	if err != nil {
		fmt.Println("Error opening file:", err)
		return
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for !found && scanner.Scan() {
		ip = scanner.Text()

		s, err := net.Dial("tcp", fmt.Sprintf("%s:%d", ip, port))
		if err != nil {
			fmt.Println("Error connecting to server:", err)
			continue
		}

		// Ask for the requested file, max depth (calls to further servers) = 3
		fmt.Fprintf(s, "2\n%s\n", os.Args[1])

		// Bring back the file, if any
		f, err := os.Create("." + string(os.PathSeparator) + os.Args[1])
		if err != nil {
			fmt.Println("Error creating file:", err)
			continue
		}

		found = copyStream(s, f)
		f.Close()
		if !found {
			os.Remove(f.Name())
		}

		s.Close()
	}

	if scanner.Err() != nil {
		fmt.Println("Error reading file:", scanner.Err())
	}
}

func copyStream(src net.Conn, dest *os.File) bool {
	_, err := io.Copy(dest, src)
	return err == nil
}
