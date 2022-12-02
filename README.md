### Before running
1) Make sure you are using Java version 8 (jdk8)

### Usage
1) Prepare a text file with name [filename] with following data.
```bash
crab
dog
fish
horse
good
spark
fly
love
peace
gentle
like
food
burger
```
2) Run the command below with the given filename with sample data. 

```bash
java -jar Btree.jar [filename]
```

3) input "commands" to see the available commands.

| Commands                 | Description                                                                                                                                                                                                         |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `commands`               | List all the available commands                                                                                                                                                                                     |
| `insert ${key}`          | Insert a key into the B+ tree                                                                                                                                                                                       |
| `delete ${key}`          | Delete a key from the B+ tree                                                                                                                                                                                       |
| `search ${key1} ${key2}` | Search for keys between `key1` and `key2`. if `key1 < key2` lexicographically, return `[key1, key2]` in ***ascending*** order. if `key1 > key2` lexicographically, return `[key1, key2]` in ***descending*** order. |
| `print`                  | Print the B+ tree                                                                                                                                                                                                   |
| `stats`                  | Print the statistics of the B+ tree                                                                                                                                                                                 |
| `quit`                   | Terminate the program                                                                                                                                                                                               |

