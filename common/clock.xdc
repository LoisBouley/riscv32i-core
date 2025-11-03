# Clock signal
create_clock -add -name sys_clk_pin -period 8.00 -waveform {0 4} [get_ports { clock }];

set_input_delay -clock sys_clk_pin 0.000 [all_inputs]
set_output_delay -clock sys_clk_pin 0.000 [all_outputs]
