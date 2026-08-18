% The code below is for the MATLAB version of this project, and it was completed between late November 2024 and early December 2024.

%----------------------------------------------------------------------
MikeysArduino = arduino('COM5', 'Nano3');
reallyDryValue = 3.5; % dry value for sensor is 3.5 V
moistureThreshold = 2.9; % soil is wet at 2.9 V
saturatedValue = 2.6; % soil is saturated with water at 1.7 V
figure(1)
max_samples = 100;
curve = animatedline('Color','b','LineWidth',1);
tic;
grid on;
for i = 1: max_samples
    currentVoltageValue(i) = readVoltage(MikeysArduino,"A1");
    time_data(i) = toc;
    addpoints(curve, time_data(i), currentVoltageValue(i))
    drawnow
    if (currentVoltageValue(i) >= reallyDryValue || currentVoltageValue(i) >= moistureThreshold)         
        disp("State1: Plant is dry. Time to water the plant!") 
        writeDigitalPin(MikeysArduino,'D2',1)
    elseif (currentVoltageValue(i) <= moistureThreshold && currentVoltageValue(i) > saturatedValue)
       disp("state2: Plant is wet but not enough. Time to water the plant!") 
       writeDigitalPin(MikeysArduino,'D2',1)
    elseif (currentVoltageValue(i) <= saturatedValue)  
       disp("state3: Plant is wet enough. No more is water is needed. Stop watering the plant!")
       writeDigitalPin(MikeysArduino,'D2',0) % pump starts to stop spinning
       pause(5); % prevents the pump to bring up anymore water in a duration of seconds
    end
end
pause(0.2); % pause for 0.2 seconds
%plot(time_data,currentVoltageValue)
xlabel("Time(s)")
ylabel("Voltage(V) of Soil Moisture Sensor in Plant")
title("Soil Moisture Voltage vs Time taken to Water Plant")

%--------------part 2------------------------------------------
figure(2)
% Moistureest represents the estimated moisture in the sensor
% slope of the graph(voltage rate) = ((2.64 + 2.58)/2) -2.605)/8 = -6.25 x 10^-4
% y-intercept = 2.605(in volts) + (6.25 x 10^-4)(8) = 2.61
Moistureest = (-6.25 * 10^-4).* time_data + 2.61;
% this equation applies to the characteristic equation for the graph that I shown in the lab report
plot(time_data, Moistureest)
title("Characteristic Equation for the Graph from my Lab Report");
xlabel("Time(s)")
ylabel("Estimated moisture(in volts) of Soil Moisture Sensor in Plant")

