build:
	dart compile exe ./bin/multi-youtube-dl.dart

install: build
	cp ./bin/multi-youtube-dl.exe ${HOME}/Scripts/multi-youtube-dl