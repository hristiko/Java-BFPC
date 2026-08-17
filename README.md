# Brute Force Password Cracker

A Java-based password cracking application that supports **brute-force** and **dictionary attacks** using three different execution modes:

* Sequential
* Parallel
* Distributed

The application accepts an MD5 or SHA-256 password hash and attempts to find the original password. A graphical interface allows the user to configure the attack and monitor its progress.

The main purpose of the project is to compare how sequential, multithreaded, and distributed approaches behave as the password search space becomes larger.

## Features

* Brute-force password cracking
* Dictionary-based password cracking
* MD5 hashing
* SHA-256 hashing
* Sequential execution
* Parallel execution using 20 threads
* Distributed execution using MPI
* Configurable character sets:

  * Lowercase letters
  * Uppercase letters
  * Digits
  * Special characters
* Configurable password length
* Real-time progress display
* Displays the cracked password and execution time

## Program Structure

The main application is organized around three core components:

### `Main.java`

Entry point of the program. It initializes the application and starts the main controller.

### `Helper.java`

Acts as the controller between the graphical interface and the cracking implementations.

It reads the selected configuration, including:

* Attack type
* Execution mode
* Hash algorithm
* Password length
* Character set
* Target hash

It then starts the appropriate cracking implementation.

### `Gui.java`

Provides the graphical user interface used to configure and monitor the cracking process.

The GUI allows the user to select the cracking parameters, start the attack, monitor its progress, and view the final result.

## Execution Modes

### Sequential

The sequential implementation performs the cracking process using a single thread.

For brute force, candidate passwords are generated and hashed using the selected algorithm. Each resulting hash is compared with the target hash until a match is found or the search space is exhausted.

Sequential execution has little computational overhead and therefore performs well when the search space is small.

### Parallel

The parallel implementation uses **20 threads**.

For brute-force attacks, multiple worker threads generate and test candidate passwords simultaneously. An `AtomicBoolean` is used to communicate when one of the workers finds the correct password.

For dictionary attacks, the dictionary is divided into separate ranges so that different threads can process different passwords simultaneously.

### Distributed

The distributed implementation uses MPI and divides the workload between:

* One ROOT process
* Three WORKER processes

The ROOT distributes the cracking configuration and coordinates the workers.

For brute force, each worker receives a separate range of password indices. Candidate passwords are generated using an index-based method similar to positional number representation, where the character set size acts as the numerical base.

This allows workers to process separate regions of the search space without generating the same passwords.

MPI operations such as `Bcast`, `Allreduce`, `Send`, and `Recv` are used for communication and synchronization.

## Brute-Force Workflow

The general cracking process is:

1. Enter the target password hash.
2. Select MD5 or SHA-256.
3. Select the password length.
4. Select the allowed character sets.
5. Select brute force or dictionary attack.
6. Select sequential, parallel, or distributed execution.
7. Start the cracking process.
8. Candidate passwords are generated and hashed.
9. Each generated hash is compared with the target hash.
10. The program displays the password and execution time when a match is found.

## Results

Brute-force experiments were performed using **digit-only passwords**.

Digits were used because they provide a relatively small character set, making it possible to consistently test increasing password lengths on the available hardware.

The experiments compared sequential, parallel, and distributed execution.

| Password Length |  Sequential |                                           Parallel |                                        Distributed |
| --------------- | ----------: | -------------------------------------------------: | -------------------------------------------------: |
| 3               |    < 250 ms | Slower than sequential for this small search space | Slower than sequential for this small search space |
| 4               |   2.1–3.0 s |                                       Mostly < 1 s |                                        ~430–450 ms |
| 5               | ~234–308+ s |                                      Mostly < 17 s |                                           ~3–4.5 s |
| 6               |  Not tested |                                         Not tested |                                            ~9–17 s |

### Performance Comparison

![Brute-force performance comparison](results/brute-force-performance.png)

### Zoomed Performance Comparison

![Zoomed brute-force performance comparison](results/brute-force-performance-zoomed.png)

### Observations

For a password length of **3**, sequential execution was the fastest. The search space was small enough that the overhead introduced by threads and inter-process communication did not provide an advantage.

At a password length of **4**, parallel and distributed execution became substantially faster. Sequential execution required approximately 2.1–3 seconds, while distributed execution completed the tests in approximately 430–450 milliseconds.

The difference became much larger at a password length of **5**. Sequential execution required approximately 234–308 seconds, while the parallel implementation generally remained below 17 seconds. Distributed execution completed the tests in approximately 3–4.5 seconds.

For a password length of **6**, only distributed execution was tested because the other approaches were considered impractical for the available testing environment. The distributed implementation completed these tests in approximately 9–17 seconds.

The experiments therefore show that the additional overhead of parallel and distributed processing is not useful for very small search spaces. As the search space increases, dividing the work becomes increasingly beneficial. In the reported experiments, the distributed implementation provided the best performance for the larger tested password lengths.

## Technologies

* Java
* Java multithreading
* MPI
* MD5
* SHA-256
* Graphical user interface
* Atomic synchronization primitives

## Testing Note

The reported execution times are specific to the hardware and test configuration used for this project. They should not be interpreted as general cracking speeds for every machine.

The performance experiments also used digit-only passwords. Adding lowercase letters, uppercase letters, or special characters substantially increases the number of possible combinations.

## Disclaimer

This project was developed for educational purposes to study brute-force algorithms, concurrency, distributed computing, hashing, and the effect of increasing search-space size.

It should only be used on passwords, hashes, and systems for which you have authorization.

## Author

**Hristijan Kochovski**
UP FAMNIT
