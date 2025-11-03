# Input arguments
set DEVICE         [lindex $argv 0]
set TOP            [lindex $argv 1]
set REP            [lindex $argv 2]
set TARGET         [lindex $argv 3]
set MEM_FILE       [file normalize [lindex $argv 4]]

source common/reportCriticalPaths.tcl

#lecture de tous les sv générés
set files [glob RTL/${REP}.${TOP}/*.sv]
read_verilog -sv $files
#ajout de clock à 10 MHz et 125 MHz
read_verilog -sv src/main/resources/vsrc/clk_wiz.v
read_verilog -sv src/main/resources/vsrc/dpram.v

set_param general.maxThreads 4
# Reading constraint file (.xdc file)
read_xdc common/${TOP}.xdc

# Detect XPM memory
auto_detect_xpm

# Start synthesis
synth_design -top ${TOP} -part ${DEVICE} -fanout_limit 100 
#-flatten_hierarchy none 
#-directive RuntimeOptimized

# Run logic optimization
opt_design

# Placing
place_design 
#-directive Quick
#write_checkpoint -force place_design.dcp

# Routing
route_design -ultrathreads
# -directive Quick 
#write_checkpoint -force route_design.dcp

# Reports
report_timing            -file ${TOP}_timing.rpt
report_timing_summary -max_paths 500 -nworst 1 -input_pins -file ${TOP}_timing_summary.rpt
report_utilization       -file ${TOP}_utilization_opt.rpt
report_critical_paths	 	   ${TOP}_critpath_report.csv
report_route_status		 -file ${TOP}_route_status.rpt
report_design_analysis   -file ${TOP}_design_analysis.rpt
report_io                -file ${TOP}_io_opt.rpt
report_drc -file ${TOP}_drc_route.rpt
report_clock_interaction -file ${TOP}_clock_interaction_opt.rpt

# Generate MMI map
write_mem_info -force ${TOP}.mmi

# Create bitstream
write_bitstream -force ${TARGET}

write_checkpoint -force bitstream_design.dcp

exit
