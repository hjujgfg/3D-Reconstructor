file = open("result.txt")
sum = 0.0
max = -1000000.0
min = 1000000.0
i = 0
for line in file:
	s = line.split(";")[2]
	d = s.split(":")
	z = float(d[2].strip())
	sum += z
	if max < z:
		max = z
	if min > z:
		min = z
	i += 1
print "Sum", sum
print "Avg", sum / i
print "Min", min
print "Max", max