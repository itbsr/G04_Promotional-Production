DISTPATH=./dist
COPYPATH=../../src/main/webapp

echo "Editing index.html to update asset paths..." &&
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS
  sed -i '' 's/\/assets/assets/g' $DISTPATH/index.html
else
  # Linux
  sed -i 's/\/assets/assets/g' $DISTPATH/index.html
fi &&
cp -v $DISTPATH/index.html $COPYPATH/WEB-INF/html/table.html &&
cp -Rv $DISTPATH/assets/* $COPYPATH/assets/ &&
echo "Files copied to webapp successfully."
