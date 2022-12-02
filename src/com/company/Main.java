package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Main {
    final static int ERR_NO_PATH = 1, ERR_NO_DIRECTORY = 2, ERR_PERMISSIONS = 3, ERR_ARGUMENT = 4, ERR_EXEC = 5, ERR_NO_FILE = 6, ERR_NO_COMMAND = 7;

    private static ArrayList<String> getArgsByLine(String line) { //возвращает ArrayList из аргументов который вводит пользователь
        ArrayList<String> args = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); ++i) {
            char x = line.charAt(i);
            if (x == 32) {
                if (!cur.toString().equals("")) {
                   args.add(cur.toString());
                }
                cur = new StringBuilder();
            } else {
                cur.append(x);
            }
        }
        if (!cur.toString().equals("")) args.add(cur.toString());
        return args;
    }

    public static int isDirectory(String userPath) {
        Path path = Paths.get(userPath);
        if (!Files.exists(path)) return ERR_NO_PATH;  //путь не существует
        if (!Files.isDirectory(path)) return ERR_NO_DIRECTORY; //путь не папка
        return 0;
    }

    public static int listDirectory(String userPath) { //возвращает номер ошибки
        int ret = isDirectory(userPath);
        if (ret > 0) return ret;
        File f = new File(userPath);
        File[] files = f.listFiles();
        assert files != null;
        for (File file : files) {
            System.out.print(file.getName() + " ");
        }
        System.out.println();
        return 0;
    }

    public static void handleDirectoryCommand(String userPath) {
        int ret = isDirectory(userPath);
        if (ret == 0) {
            System.out.println("true");
            return;
        }
        if (ret == ERR_NO_PATH) {
            handleError(ret);
            return;
        }
        System.out.println("false");
    }

    public static int handleDefineCommand(String userPath) {
        int ret = isDirectory(userPath);
        if (ret == ERR_NO_PATH) return ret;
        if (ret == ERR_NO_DIRECTORY) {
            System.out.println("File");
        } else {
            System.out.println("Directory");
        }
        return 0;
    }

    public static String getFileExtension(String s) {
        return s.substring(s.lastIndexOf(".") + 1);
    }

    public static int listPythonFiles(String userPath) { //возвращает номер ошибки
        int ret = isDirectory(userPath);
        if (ret > 0) return ret;
        File f = new File(userPath);
        File[] files = f.listFiles();
        assert files != null;
        for (File file : files) {
            if (getFileExtension(file.getName()).equals("py")) {
                System.out.print(file.getName() + " ");
            }
        }
        System.out.println();
        return ret;
    }

    public static int printPermissions(String userPath) {
        File file = new File(userPath);
        if (!file.exists()) return ERR_NO_PATH;
        String permissions = "";
        if (file.canRead()) permissions += "r";
        else permissions += "-";
        if (file.canWrite()) permissions += "w";
        else permissions += '-';
        if (file.canExecute()) permissions += "x";
        else permissions += '-';
        System.out.println(permissions);
        return 0;
    }

    public static boolean checkArguments(ArrayList<String> userArgs) {
        if (userArgs.isEmpty()) return false;
        int num = 2;
        if (userArgs.get(0).equals("setmod")) num = 3;
        return userArgs.size() == num;
    }

    public static boolean checkPermissionsChar(char cur, char target) {
        return (cur != target && cur != '-');
    }

    public static int checkPermissions(String perm) {
        if (perm.length() != 3 || checkPermissionsChar(perm.charAt(0), 'r') || checkPermissionsChar(perm.charAt(1), 'w') || checkPermissionsChar(perm.charAt(2), 'x')) return ERR_PERMISSIONS;
        return 0;
    }

    public static int setPermissions(String userPath, String perm) {
        File file = new File(userPath);
        if (!file.exists()) return ERR_NO_PATH;
        int ret = checkPermissions(perm);
        if (ret == 0) {
            boolean err = file.setReadable(perm.charAt(0) != '-') | file.setWritable(perm.charAt(1) != '-') | file.setExecutable(perm.charAt(2) != '-');
            if (!err) ret = ERR_EXEC;
        }
        return ret;
    }

    public static ArrayList<String> getContent(File file) {
        ArrayList<String> res = new ArrayList<>();
        try {
            Scanner it = new Scanner(file);
            while (it.hasNextLine()) {
                res.add(it.nextLine());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

    public static int printContent(String userPath) {
        File file = new File(userPath);
        if (!file.exists()) return ERR_NO_PATH;
        if (file.isDirectory()) return ERR_NO_FILE;
        ArrayList<String> content = getContent(file);
        for (String line : content) {
            System.out.println(line);
        }
        return 0;
    }

    public static int appendFooter(String userPath) {
        File file = new File(userPath);
        if (!file.exists()) return ERR_NO_PATH;
        if (file.isDirectory()) return ERR_NO_FILE;
        try(FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
            out.print("\n# Autogenerated line");
        } catch (IOException e) {
            System.out.println(e);
        }
        return 0;
    }
    public static void deleteFile(File file) {
        try {
            if (file.isDirectory()) {
                File[] entries = file.listFiles();
                if (entries != null) {
                    for (File entry : entries) {
                        deleteFile(entry);
                    }
                }
            }
            else if (!file.delete()) throw new Exception("Failed to delete" + file);
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void copyBackup(File dest, File backup) {
        try {
            Files.copy(Path.of(dest.getPath()), Path.of(backup.getPath()));
            if (dest.isDirectory()) {
                File[] entries = dest.listFiles();
                if (entries != null) {
                    for (File entry : entries) {
                        try {
                            Files.copy(Path.of(entry.getPath()), Path.of(backup.getPath() + "/" + entry.getPath()));
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static int createBackup(String userPath) {
        File file = new File(userPath);
        if (!file.exists()) return ERR_NO_PATH;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy");
        Date date = new Date();
        String currentDate = formatter.format(date);
        File backupFile = new File("/tmp/" + currentDate + ".backup");
        if (backupFile.exists()) deleteFile(backupFile);
        copyBackup(file, backupFile);
        return 0;
    }

    public static int printLongestWord(String userPath) {
        File file = new File(userPath);
        if (!file.exists()) return ERR_NO_PATH;
        if (file.isDirectory()) return ERR_NO_FILE;
        ArrayList<String> content = getContent(file);
        String longestWord = "";
        for (String line : content) {
            String[] words = line.split(" ");
            for (String word : words) {
                if (word.length() > longestWord.length()) {
                    longestWord = word;
                }
            }
        }
        System.out.println(longestWord);
        return 0;
    }

    public static void help() {
        System.out.println("MyFS 1.0 команды:\n" +
                "ls <path>               выводит список всех файлов и директорий для `path`\n" +
                "ls_py <path>            выводит список файлов с расширением `.py` в `path`\n" +
                "is_dir <path>           выводит `true`, если `path` это директория, в других случаях `false`\n" +
                "define <path>           выводит `директория` или `файл` в зависимости от типа `path`\n" +
                "readmod <path>          выводит права для файла в формате `rwx` для текущего пользователя\n" +
                "setmod <path> <perm>    устанавливает права для файла `path`\n" +
                "cat <path>              выводит контент файла\n" +
                "append <path>           добавляет строку `# Autogenerated line` в конец `path`\n" +
                "bc <path>               создает копию `path` в директорию `/tmp/${date}.backup` где, date - это дата в формате `dd-mm-yyyy`\n" +
                "greplong <path>         выводит самое длинное слово в файле\n" +
                "help                    выводит список команд и их описание\n" +
                "exit                    завершает работу программы");
    }

    public static void handleError(int ret) {
        switch (ret) {
            case ERR_NO_PATH -> System.out.println("ERROR: Path does not exist");
            case ERR_NO_DIRECTORY -> System.out.println("ERROR: Path does not lead to directory");
            case ERR_ARGUMENT -> System.out.println("ERROR: Inappropriate number of arguments");
            case ERR_PERMISSIONS -> System.out.println("ERROR: inappropriate format of permissions");
            case ERR_EXEC -> System.out.println("ERROR: Something occured while executing");
            case ERR_NO_FILE -> System.out.println("ERROR: Input should be file");
            case ERR_NO_COMMAND -> System.out.println("ERROR: Unknown command");
        }
    }

    public static void main(String[] args) {
        System.out.println("MyFS 1.0 команды:\n" +
                "ls <path>               выводит список всех файлов и директорий для `path`\n" +
                "ls_py <path>            выводит список файлов с расширением `.py` в `path`\n" +
                "is_dir <path>           выводит `true`, если `path` это директория, в других случаях `false`\n" +
                "define <path>           выводит `директория` или `файл` в зависимости от типа `path`\n" +
                "readmod <path>          выводит права для файла в формате `rwx` для текущего пользователя\n" +
                "setmod <path> <perm>    устанавливает права для файла `path`\n" +
                "cat <path>              выводит контент файла\n" +
                "append <path>           добавляет строку `# Autogenerated line` в конец `path`\n" +
                "bc <path>               создает копию `path` в директорию `/tmp/${date}.backup` где, date - это дата в формате `dd-mm-yyyy`\n" +
                "greplong <path>         выводит самое длинное слово в файле\n" +
                "help                    выводит список команд и их описание\n" +
                "exit                    завершает работу программы");
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();
                ArrayList<String> userArgs = getArgsByLine(line);
                if (userArgs.get(0).equals("exit")) {
                    System.out.println("GoodBye");
                    break;
                }
                if (!checkArguments(userArgs)) {
                    handleError(ERR_ARGUMENT);
                    continue;
                }
                int ret = 0;
                switch (userArgs.get(0)) {
                    case "ls" -> ret = listDirectory(userArgs.get(1));
                    case "ls_py" -> ret = listPythonFiles(userArgs.get(1));
                    case "is_dir" -> handleDirectoryCommand(userArgs.get(1));
                    case "define" -> ret = handleDefineCommand(userArgs.get(1));
                    case "readmod" -> ret = printPermissions(userArgs.get(1));
                    case "setmod" -> ret = setPermissions(userArgs.get(1), userArgs.get(2));
                    case "cat" -> ret = printContent(userArgs.get(1));
                    case "append" -> ret = appendFooter(userArgs.get(1));
                    case "bc" -> ret = createBackup(userArgs.get(1));
                    case "greplong" -> ret = printLongestWord(userArgs.get(1));
                    case "help" -> help();
                    default -> ret = ERR_NO_COMMAND;
                }
                handleError(ret);
            }
        } catch (Exception e) {
            System.out.printf("%s", e);
        }
    }
}
