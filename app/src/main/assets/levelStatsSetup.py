prefix = 'INSERT INTO LevelStats VALUES ('
outs = []
outs.append('ContentValues insertValues = new ContentValues();')
with open('populateLevelStats.txt', 'r') as f:
	lines = f.readlines()
	for line in lines:
		line = [int(n) for n in line.strip()[len(prefix):-1].split(', ')]
		out = 'insertValues.put("level", {});\n'.format(line[0])
		out += 'insertValues.put("children", {});\n'.format(line[1])
		out += 'insertValues.put("columns", {});\n'.format(line[2])
		out += 'insertValues.put("time", {});\n'.format(line[3])
		out += 'mDB.insert("LevelStats", null, insertValues);\n'
		out += 'insertValues.clear();\n'
		print(out)
		outs.append(out)
with open('populateLevelStats2.txt', 'w') as f:
	for out in outs:
		f.write(out)