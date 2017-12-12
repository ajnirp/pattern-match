prefix = 'INSERT INTO Children VALUES ('
outs = []
outs.append('ContentValues insertValues = new ContentValues();')
with open('populateChildren.txt', 'r') as f:
	lines = f.readlines()
	for line in lines:
		line = line.strip()[len(prefix):-1].split(', ')
		out = 'insertValues.put("cid", {});\n'.format(int(line[0]))
		out += 'insertValues.put("kmer", {});\n'.format(line[1].replace("'", '"'))
		out += 'mDB.insert("Children", null, insertValues);\n'
		out += 'insertValues.clear();\n'
		print(out)
		outs.append(out)
with open('populateChildren2.txt', 'w') as f:
	for out in outs:
		f.write(out)