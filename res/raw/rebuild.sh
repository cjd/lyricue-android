for SVG in ic_*.svg collapsed.svg expanded.svg
do PNG=`basename $SVG svg`png
inkscape -z  -e ../drawable-xhdpi/$PNG -w 96 -h 96 $SVG
inkscape -z  -e ../drawable-hdpi/$PNG -w 72 -h 72 $SVG
inkscape -z  -e ../drawable-mdpi/$PNG -w 48 -h 48 $SVG
inkscape -z  -e ../drawable-ldpi/$PNG -w 36 -h 36 $SVG
done
inkscape -z  -e ../drawable-hdpi/lyricue_header.png -w 640 lyricue_header.svg
inkscape -z  -e ../drawable-ldpi/lyricue_header.png -w 320 lyricue_header.svg
inkscape -z  -e ../drawable-mdpi/lyricue_header.png -w 426 lyricue_header.svg
inkscape -z  -e ../drawable-xhdpi/lyricue_header.png -w 853 lyricue_header.svg
for SIZE in hdpi ldpi mdpi xhdpi
do convert ../drawable-$SIZE/lyricue_header.png -rotate 270 ../drawable-$SIZE/lyricue_header_rotated.png
done
