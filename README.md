# ⚙️ Cloud Load Balancer (Java + Docker)

A distributed **file storage system** with a custom-built load balancer that ensures **reliability, performance, and scalability**.  
This project was developed as part of my university coursework to simulate real-world cloud infrastructure and scheduling challenges.

---

## 🚀 Features
- **Cloud Infrastructure with Docker & Docker Compose**  
  - 4 file storage containers  
  - 1 load balancer  
  - MySQL server  
  - Local SQLite database  
  - JavaFX-based user portal  

- **Scheduling Algorithms Implemented**
  - First-Come First-Serve (FCFS)  
  - Shortest Job Next (SJN)  
  - Priority Scheduling  
  - Shortest Remaining Time (SRT)  
  - Round Robin (RR)  
  - Multi-Level Queue Scheduling  

- **System Reliability**
  - Artificial delays (30–90s) to simulate real-world latency  
  - File locking mechanisms to preserve data integrity during concurrent operations  
  - Load distribution across multiple containers  

- **User Management**
  - Standard and admin roles  
  - Access controlled via a **JavaFX GUI portal**  

---

## 🛠️ Tech Stack
- **Languages:** Java, SQL  
- **Frameworks & Tools:** JavaFX, Docker, Docker Compose  
- **Databases:** MySQL, SQLite  

---


## 🔧 Installation & Usage
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/cloud-load-balancer.git
   cd cloud-load-balancer
