# Gephi4AcademicMap
These are the codes for generating maps on Acemap via Gephi.

You need to create a folder in user dir (create a new folder in Gephi4AcademicMap folder), and all maps generated will be put there.

<papermap.java> is for generating papermaps in all fields, previous maps are like this: https://acemap.info/papermap/demoSVG?topicID=0271BC14. This code can run immediately and regenerate all the maps in all fileds.

<confauthor.java> is for generating author maps in CCF conferences, previous maps are like this: https://acemap.info/confmap?ConfID=43226B44. To retrieve edges (relationship of author), you need to open a port in the server, and send post to the server, which you may want to ask the groupleader for help. The output includes a txt file, which lists all the authors that are filtered by our algorithm in this conference and do not show in the middle of the map. Previously, we'd like two ways of coloring, so generateSVG has two parameters, if the second one is 1, then two ways of coloring are provided. However, Acemap does not support showing all the two colors so far, so I suggest that generateSVG(ConferenceID, 0) is enough.

<ConferenceCompareAuthor.java> is for comparing authors in two conferences. red and green represents two conferences, and yellow represents authors that have papers in both conferences. For input, you should input generateSvg(ConferenceID1, ConferenceID2). Sample maps are like this: https://acemap.info/CCFConfCompare. This code can run immediately.

<ConfComparePaper.java> is for comparing papers in two conferences. Sample maps are like this: https://acemap.info/CCFConfCompare. This code can run immediately.

<kdd2018.java> is for generating authormaps in conference in a certain year, for example, to generate a map of all authors with papers accepted in kdd2018. For input, you need a txt file which contains all authors in this conference. In this txt file, every line contains authors in the same paper accepted, namely coauthors. 


