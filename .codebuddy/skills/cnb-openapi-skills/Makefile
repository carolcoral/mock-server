.PHONY: docs

docs:
	@echo "Generating docs..."
	#curl https://cnb.cool/cnb/sdk/cnb-sdk-generator/-/git/raw/master/install.sh -sSfL | sh
	rm -rf references/*
	cnb-sdk-generator -l skill -i https://api.cnb.cool/swagger.json -o references
	cp -rf docs/ext/ references/
	@bash scripts/update-version.sh

npm:
	@echo "publish npm packages..."
	npm config set registry https://npm.cnb.cool/cnb/sdk/npm/-/packages
	npm publish