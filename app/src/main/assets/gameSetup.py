prefix = 'INSERT INTO Games VALUES ('
outs = []
outs.append('ContentValues insertValues = new ContentValues();')
with open('populateGame.txt', 'r') as f:
	lines = f.readlines()
	for line in lines:
		line = [int(n) for n in line.strip()[len(prefix):-1].split(', ')]
		out = 'insertValues.put("gid", {});\n'.format(line[0])
		out += 'insertValues.put("cid", {});\n'.format(line[1])
		out += 'insertValues.put("level", {});\n'.format(line[2])
		out += 'mDB.insert("Games", null, insertValues);\n'
		out += 'insertValues.clear();\n'
		print(out)
		outs.append(out)
with open('populateGame2.txt', 'w') as f:
	for out in outs:
		f.write(out)