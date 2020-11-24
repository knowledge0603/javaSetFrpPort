import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Command {
    public static void exeCmd(String commandStr) {
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(commandStr);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
                System.out.println(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeFile(int port) {
        //Windows is written as follows
        //String fileName=PathUtil.getCurrentPath()+"frps"+"_"+port+".ini";
        //Linux is written as follows
        String fileName = "frps" + "_" + port + ".ini";
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write("[common]\n");
            writer.write("bind_port = " + port + "\n");
            writer.write("vhost_http_port = " + (port - 1000));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean processBuilder(int port) throws IOException {
        writeFile(port);
        //Create the ProcessBuilder object
        ProcessBuilder processBuilder = new ProcessBuilder();
        //Windows below the way to call
        //processBuilder.command(PathUtil.getCurrentPath()+"frps.exe","-c",PathUtil.getCurrentPath()+"frps"+"_"+port+".ini");
        //Linux makes the following calls
        try {
            run_command(partitionCommandLine("./frps -c ./frps_" + port + ".ini"), PathUtil.getCurrentPath());
        } catch (InterruptedException e) {
           e.printStackTrace();
           return false;
        }
        //By merging the standard input stream and the error input stream, the error and normal information output by the third party program can be obtained by reading the information from the standard input stream
        processBuilder.redirectErrorStream(true);
        //Start a process
        Process process = processBuilder.start();
        //Only the input stream is read because the front part merges the error and normal information in the input stream
        InputStream inputStream = process.getInputStream();
        //Streams bytes to character streams
        InputStreamReader reader = new InputStreamReader(inputStream, "gbk");
        //Character buffer
        char[] chars = new char[1024];
        int len = -1;
        while ((len = reader.read(chars)) != -1) {
            String string = new String(chars, 0, len);
            if (string.contains("success")) {
                inputStream.close();
                reader.close();
                return true;
            }
        }
        return false;
    }

    public static boolean processBuilderLinux(int port) throws IOException {
        writeFile(port);
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {
            run_command(partitionCommandLine("./frps -c ./frps_" + port + ".ini"), PathUtil.getCurrentPath());
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean run_command(final String[] command, final String work_path) throws IOException, InterruptedException {
        List<String> result_list = new ArrayList<>();
        ProcessBuilder hiveProcessBuilder = new ProcessBuilder(command);
        File fi = new File(work_path);
        hiveProcessBuilder.directory(fi);
        hiveProcessBuilder.redirectErrorStream(true);
        Process hiveProcess = hiveProcessBuilder.start();
        BufferedReader std_input = new BufferedReader(new InputStreamReader(hiveProcess.getInputStream(), "UTF-8"));
        BufferedReader std_error = new BufferedReader(new InputStreamReader(hiveProcess.getErrorStream(), "UTF-8"));
        String line;
        while ((line = std_input.readLine()) != null) {
            result_list.add(line);
            if (line.contains("success")) {
                return true;
            }
        }
        while ((line = std_error.readLine()) != null) {
            return false;
        }
        hiveProcess.waitFor();
        if (hiveProcess.exitValue() != 0) {
            System.out.println("failed to execute:" + command);
            return false;
        }
        System.out.println("execute success:" + command);
        return true;
    }

    /**
     * Process the command
     */

    public static String[] partitionCommandLine(final String command) {
        final ArrayList<String> commands = new ArrayList<>();
        int index = 0;
        StringBuffer buffer = new StringBuffer(command.length());
        boolean isApos = false;
        boolean isQuote = false;
        while (index < command.length()) {
            final char c = command.charAt(index);
            switch (c) {
                case ' ':
                    if (!isQuote && !isApos) {
                        final String arg = buffer.toString();
                        buffer = new StringBuffer(command.length() - index);
                        if (arg.length() > 0) {
                            commands.add(arg);
                        }
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '\'':
                    if (!isQuote) {
                        isApos = !isApos;
                    } else {
                        buffer.append(c);
                    }
                    break;
                case '"':
                    if (!isApos) {
                        isQuote = !isQuote;
                    } else {
                        buffer.append(c);
                    }
                    break;
                default:
                    buffer.append(c);
            }
            index++;
        }
        if (buffer.length() > 0) {
            final String arg = buffer.toString();
            commands.add(arg);
        }
        return commands.toArray(new String[commands.size()]);
    }
}
