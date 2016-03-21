file = open("result.txt")
sum = 0.0
max = -1000000.0
min = 1000000.0
i = 0
mincounter = 0
maxcounter = 0
number = 0.
for line in file:
	number += 1
	s = line.split(";")[2]
	d = s.split(":")
	z = float(d[2].strip())
	sum += z
	if max < z:
		max = z
	if min > z:
		min = z
	i += 1
	if z > 10:
		maxcounter += 1
	if z < -1:
		mincounter += 1
print "Sum", sum
print "Avg", sum / i
print "Min", min
print "Max", max
print "Min cntr", mincounter
print "Max cntr", maxcounter
print "Total number", number