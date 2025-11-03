# Input arguments
set DEVICE         [lindex $argv 0]
set TOP            [lindex $argv 1]
set REP            [lindex $argv 2]

#lecture de tous les sv générés
set files [glob RTL/${REP}.${TOP}/*.sv]
read_verilog -sv $files
read_verilog -sv src/main/resources/vsrc/clk_wiz.v
read_verilog -sv src/main/resources/vsrc/dpram.v

read_xdc common/clock.xdc
# Detect XPM memory
auto_detect_xpm

# Start synthesis
synth_design -top ${TOP} -part ${DEVICE} -mode "out_of_context"
report_utilization -file ${TOP}_utilization.rpt
report_timing -file ${TOP}_timing.rpt
report_clocks

get_ports *

set filename "${TOP}_summary.rpt"
set fileId [open $filename "w"]
if { [get_clocks] != "" } {
	puts -nonewline $fileId "Clock  | " 
	puts $fileId [get_property -min PERIOD [get_clocks]];
}
if { [get_timing_paths] != "" } {
	puts -nonewline $fileId "Slack  | "
	puts $fileId [get_property SLACK [get_timing_paths]];
}
close $fileId
exit
