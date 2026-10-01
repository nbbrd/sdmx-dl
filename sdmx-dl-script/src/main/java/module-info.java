import sdmxdl.script.spi.ScriptGenerator;
import sdmxdl.script.std.bash.BashCliScriptGenerator;
import sdmxdl.script.std.bash.BashRestScriptGenerator;
import sdmxdl.script.std.batch.BatchCliScriptGenerator;
import sdmxdl.script.std.jbang.JBangJavaScriptGenerator;
import sdmxdl.script.std.jupyter.JupyterCliScriptGenerator;
import sdmxdl.script.std.jupyter.JupyterRestScriptGenerator;
import sdmxdl.script.std.powerquery.PowerQueryRestScriptGenerator;
import sdmxdl.script.std.powershell.PowerShellCliScriptGenerator;
import sdmxdl.script.std.powershell.PowerShellRestScriptGenerator;
import sdmxdl.script.std.python.PythonCliScriptGenerator;
import sdmxdl.script.std.python.PythonRestScriptGenerator;
import sdmxdl.script.std.r.RCliScriptGenerator;
import sdmxdl.script.std.r.RRestScriptGenerator;

module sdmxdl.script.std {
    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires sdmxdl.api;
    requires com.github.mustachejava;

    provides ScriptGenerator with
            BashCliScriptGenerator,
            BashRestScriptGenerator,
            BatchCliScriptGenerator,
            JBangJavaScriptGenerator,
            JupyterCliScriptGenerator,
            JupyterRestScriptGenerator,
            PowerQueryRestScriptGenerator,
            PowerShellCliScriptGenerator,
            PowerShellRestScriptGenerator,
            PythonCliScriptGenerator,
            PythonRestScriptGenerator,
            RCliScriptGenerator,
            RRestScriptGenerator;
}
