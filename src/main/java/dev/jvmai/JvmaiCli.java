package dev.jvmai;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "jvmai", mixinStandardHelpOptions = true, version = "jvmai 1.0.0",
        description = "AI-Assisted JVM Diagnostic CLI Tool",
        subcommands = {
            DiagnoseCommand.class
        })
public class JvmaiCli implements Runnable {

    @Override
    public void run() {
        // if no subcommands are matched, show help
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JvmaiCli()).execute(args);
        System.exit(exitCode);
    }
}
