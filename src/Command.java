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

    public static void main(String[] args) throws IOException {
        /*
        String commandStr = "frpc -c  frpc.ini";
        //String commandStr = "ipconfig";*/
        // Command.exeCmd("G:\\work\\20200914\\command\\out\\production\\command\\frpc.exe , -c ,frpc.ini");
        //  String cmd =  PathUtil.getCurrentPath() + "frpc.exe -c  frpc.ini";
        //processBuilder();
    }

    public static void writeFile(int port) {
        //String fileName="C:\\kuka.txt";
        //windows 以下写法
        //String fileName=PathUtil.getCurrentPath()+"frps"+"_"+port+".ini";
        //linux 以下写法
        String fileName = "frps" + "_" + port + ".ini";
        try {
            //使用这个构造函数时，如果存在kuka.txt文件，
            //则先把这个文件给删除掉，然后创建新的kuka.txt
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
        //创建ProcessBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();
        //设置执行的第三方程序(命令),第一个参数是命令,之后的是参数
        //        processBuilder.command("ping","127.0.0.1");
        System.out.println("fps 服务被开启  port is:" + port);
        //System.out.println("服务器 frps.ini 路径："+PathUtil.getCurrentPath()+"frps"+"_"+port+".ini");
        //windows 以下 调用方式
         //processBuilder.command(PathUtil.getCurrentPath()+"frps.exe","-c",PathUtil.getCurrentPath()+"frps"+"_"+port+".ini");
        //linux 以下调用方式
        try {
            System.out.println("processBuilder start 1001" );

            run_command(partitionCommandLine("./frps -c ./frps_" + port + ".ini"), PathUtil.getCurrentPath());
            System.out.println("processBuilder start 1002" );
           // run_command(partitionCommandLine("frps -c frps_" + port + ".ini"), PathUtil.getCurrentPath());
        } catch (InterruptedException e) {
            System.out.println("processBuilder start 1003" );
           e.printStackTrace();
           return false;
        }
        //        processBuilder.command("java","-jar","f:/xc-service-manage-course.jar");
        //将标准输入流和错误输入流合并，通过标准输入流读取信息就可以拿到第三方程序输出的错误信息、正常信息
        processBuilder.redirectErrorStream(true);
        System.out.println("processBuilder start 1004" );
        //启动一个进程
        Process process = processBuilder.start();
        System.out.println("processBuilder start 1005" );
        //由于前边将错误和正常信息合并在输入流，只读取输入流
        InputStream inputStream = process.getInputStream();
        //将字节流转成字符流
        InputStreamReader reader = new InputStreamReader(inputStream, "gbk");
        System.out.println("processBuilder start 1006" );
        //字符缓冲区
        char[] chars = new char[1024];
        int len = -1;
        while ((len = reader.read(chars)) != -1) {
            String string = new String(chars, 0, len);
            System.out.println(string);
            System.out.println("processBuilder start 1007" );
            if (string.contains("success")) {
                System.out.println("frp 通道 开启成功！");
                inputStream.close();
                reader.close();
                return true;
            }
        }
        System.out.println("processBuilder start 1008" );
        return false;
    }

    public static boolean processBuilderLinux(int port) throws IOException {
        writeFile(port);
        //创建ProcessBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();
        //设置执行的第三方程序(命令),第一个参数是命令,之后的是参数
        //        processBuilder.command("ping","127.0.0.1");
        System.out.println("fps 服务被开启  port is:" + port);
        //System.out.println("服务器 frps.ini 路径："+PathUtil.getCurrentPath()+"frps"+"_"+port+".ini");
        //windows 以下 调用方式
        //processBuilder.command(PathUtil.getCurrentPath()+"frps.exe","-c",PathUtil.getCurrentPath()+"frps"+"_"+port+".ini");
        //linux 以下调用方式
        try {
            System.out.println("processBuilder start 1001" );
            run_command(partitionCommandLine("./frps -c ./frps_" + port + ".ini"), PathUtil.getCurrentPath());
            System.out.println("processBuilder start 1002" );
            return true;
            // run_command(partitionCommandLine("frps -c frps_" + port + ".ini"), PathUtil.getCurrentPath());
        } catch (InterruptedException e) {
            System.out.println("processBuilder start 1003" );
            e.printStackTrace();
            return false;
        }
    }

    public static boolean run_command(final String[] command, final String work_path) throws IOException, InterruptedException {
        List<String> result_list = new ArrayList<>();
        System.out.println("run_command 1001" );
        ProcessBuilder hiveProcessBuilder = new ProcessBuilder(command);
        File fi = new File(work_path);
        hiveProcessBuilder.directory(fi);
        hiveProcessBuilder.redirectErrorStream(true);
        Process hiveProcess = hiveProcessBuilder.start();
        BufferedReader std_input = new BufferedReader(new InputStreamReader(hiveProcess.getInputStream(), "UTF-8"));
        BufferedReader std_error = new BufferedReader(new InputStreamReader(hiveProcess.getErrorStream(), "UTF-8"));
        System.out.println("run_command 1002" );
        String line;
        while ((line = std_input.readLine()) != null) {
            result_list.add(line);
            System.out.println(line);
            System.out.println("run_command 1002001" );
            if (line.contains("success")) {
                System.out.println("run_command 1002002" );
                return true;
            }
        }
        System.out.println("run_command 1003" );
        while ((line = std_error.readLine()) != null) {
            System.out.println("run_command 100301" );
            System.out.println(line);
            //log.error(line);
            System.out.println("run_command 100302" );
            return false;
        }
        System.out.println("run_command 100303" );
        hiveProcess.waitFor();
        System.out.println("run_command 100304" );
        if (hiveProcess.exitValue() != 0) {
            System.out.println("failed to execute:" + command);
            return false;
        }
        System.out.println("run_command 1004" );
        System.out.println("execute success:" + command);
        return true;
    }

    /**
     * 对命令进行处理
     */

    public static String[] partitionCommandLine(final String command) {
        final ArrayList<String> commands = new ArrayList<>();
        int index = 0;
        StringBuffer buffer = new StringBuffer(command.length());
        boolean isApos = false;
        boolean isQuote = false;
        System.out.println("run_command 1005" );
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
                    System.out.println("run_command 1006" );
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
                    System.out.println("run_command 1007" );
                    break;
                default:
                    buffer.append(c);
                    System.out.println("run_command 1008" );
            }
            index++;
        }
        if (buffer.length() > 0) {
            final String arg = buffer.toString();
            commands.add(arg);
        }
        System.out.println("run_command 1009" );
        return commands.toArray(new String[commands.size()]);
    }
}
