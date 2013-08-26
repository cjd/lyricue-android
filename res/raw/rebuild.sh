for SVG in ic_*.svg collapsed.svg expanded.svg
do PNG=`basename $SVG svg`png
convert $SVG -size 96x96 -transparent black ../drawable-xhdpi/$PNG
convert $SVG -size 72x72 -transparent black ../drawable-hdpi/$PNG
convert $SVG -size 48x48 -transparent black ../drawable-mdpi/$PNG
convert $SVG -size 36x36 -transparent black ../drawable-ldpi/$PNG
done
inkscape -z  -e ../drawable-hdpi/lyricue_header.png -w 640 lyricue_header.svg
inkscape -z  -e ../drawable-ldpi/lyricue_header.png -w 320 lyricue_header.svg
inkscape -z  -e ../drawable-mdpi/lyricue_header.png -w 426 lyricue_header.svg
inkscape -z  -e ../drawable-xhdpi/lyricue_header.png -w 853 lyricue_header.svg
for SIZE in hdpi ldpi mdpi xhdpi
do convert ../drawable-$SIZE/lyricue_header.png -rotate 270 ../drawable-$SIZE/lyricue_header_rotated.png
done
