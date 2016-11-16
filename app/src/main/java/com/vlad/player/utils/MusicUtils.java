package com.vlad.player.utils;

import android.media.MediaMetadataRetriever;

import com.vlad.player.data.Song;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

public final class MusicUtils {

    private static final String mCovers[] = {
            "http://5.darkroom.shortlist.com/980/1e9f030cc5dae85abb2eec51abbd9465:f68a2c605d469680479ac720d5e091dc/the-beatles-revolver",
            "http://6.darkroom.shortlist.com/980/ece5c09a601670c59ca358295a7cea43:5a395a6ab1e65c512b569e001f8f8d62/cream-disraeli-gears",
            "http://1.darkroom.shortlist.com/980/aaee6a066a224e88c106472960ed6982:36e55928b902540e4f1ca4a6cc3edaa1/the-doors-the-doors",
            "http://2.darkroom.shortlist.com/980/4de6700b34413b8bd3fb9b051ac20ce8:b90f91283764b637d588781645670a1f/the-jimi-hendrix-experience-are-you-experienced",
            "http://3.darkroom.shortlist.com/980/b2c8abff60730acf04ce42e3b4cb34c1:c68647e08cf560e80918fb9c83c0dc3e/the-small-faces-ogden-s-gone-nut-flake",
            "http://4.darkroom.shortlist.com/980/64412f0ddc87d403a8056c3556bd74ec:158e33a7b8926904763ac57cc5b91f6e/the-band-music-from-big-pink",
            "http://5.darkroom.shortlist.com/980/31792e48968ba0eafd7030ee2b978078:d750b97af0a07875645160609c27f14f/jeff-beck-truth",
            "http://6.darkroom.shortlist.com/980/1b0523b8a6e29a97e8403b2de41d728a:4d190ad261ba48ecbea56b08fc1405e2/the-jimi-hendrix-experience-electric-ladyland",
            "http://1.darkroom.shortlist.com/980/2c71cf22d39978fe93d7915ad3be7927:e4cc059023dedf022ce808b034c8b9ac/the-beatles-abbey-road",
            "http://2.darkroom.shortlist.com/980/6527c8209a107f552c636a66f9f0be76:4a8539148dc5cd86d57159c417629fb8/led-zeppelin-ii",
            "http://3.darkroom.shortlist.com/980/05657f1d3b6259a20e3ca56875806283:599fab18e938f8595273b0b4b3c08152/creedence-clearwater-revival-green-river",
            "http://4.darkroom.shortlist.com/980/e2d177ab26ed24ef9be16aceb15a98bb:39c3660adcd4092dbacb2aad84b54f2b/the-who-who-s-next",
            "http://5.darkroom.shortlist.com/980/c3a2fd0ccf62b62b22f88afb9ab54ca3:a220c5a7cc9f3739c2bc5497a4d10479/rod-stewart-every-picture-tells-a-story",
            "http://6.darkroom.shortlist.com/980/87a20033e6d03cdb6ca5f4ccc1e96215:4b6a098a9872eec38b72744e8aeeaa59/led-zeppelin-iv",
            "http://1.darkroom.shortlist.com/980/24e544dc6632b6b391e347c4cf3623f9:852c2fc5ae8dbb54ae989d7ab966191a/the-rolling-stones-exile-on-main-street",
            "http://2.darkroom.shortlist.com/980/6c035c87a88125111cc654654af46642:5cba942cba58a123d1558386d56fd770/deep-purple-machine-head",
            "http://3.darkroom.shortlist.com/980/eddfa92a393917c4601f029d76ccd3ae:0cb323ed4572d89d205964eb69f7f511/david-bowie-ziggy-stardust-the-spiders-from-mars",
            "http://4.darkroom.shortlist.com/980/1aeb42ab90facb115eb06b0913a4b182:67c61b60a9c71cd2b234dacb947bc85a/pink-floyd-the-dark-side-of-the-moon",
            "http://5.darkroom.shortlist.com/980/6c0ecf5895568b02764f798b7b7a34fc:1597c2b5215f6b0a9d85eab2f8894b97/alice-cooper-billion-dollar-babies",
            "http://6.darkroom.shortlist.com/980/8e6421040ac1b5a5cbe6e4ffda5985b7:4acc7f4d5ade6c6af4fd16e6bf04a574/elton-john-goodbye-yellow-brick-road",
            "http://1.darkroom.shortlist.com/980/cc7a3e91fc6ab085fa7d3b9ae8c8199c:50f4e28c006262e97532659848995e5e/aerosmith-toys-in-the-attic",
            "http://2.darkroom.shortlist.com/980/7888e8198a0b5acd02128bc672fb583e:62886f81381d91e64825f085b6bbf04e/queen-a-night-at-the-opera",
            "http://3.darkroom.shortlist.com/980/3d7af93f596e7b304476fd2679c655d9:f7f14f9eec33782f09d920280bfa2623/pink-floyd-wish-you-were-here",
            "http://4.darkroom.shortlist.com/980/2ddb3d484031374fe83db25c2f2f2ac7:9d4fa9a9453ca4f076e181f233324a57/bruce-springsteen-born-to-run",
            "http://5.darkroom.shortlist.com/980/921e2257090cda619cc1585e1d45e51a:985114840bd62c18935948fe3d06b890/boston-boston",
            "http://6.darkroom.shortlist.com/980/5e52a0c4ece6799fbb4f29dc109abf00:a5ced04dd6f6db2f7f4035f4952f850e/thin-lizzy-jailbreak",
            "http://1.darkroom.shortlist.com/980/a778f9dc0b0c2637f678eb0b5949dc1a:b455b1de24196a49c03ffb1a51fdc470/the-eagles-hotel-california",
            "http://2.darkroom.shortlist.com/980/9b1b2b31831f37b64412f6cf7fe297f4:446ef1811a427fe74209d3d7ea91251d/aerosmith-rocks",
            "http://3.darkroom.shortlist.com/980/4a79e886df0120e473fcecb952ae02ce:16812fa4be41360d49912f736a52defe/meat-loaf-bat-out-of-hell",
            "http://4.darkroom.shortlist.com/980/efac99c34b4b67e980a04ff6561845dc:471eb2565557fe4e9605865f9cb6bcac/van-halen-van-halen",
            "http://5.darkroom.shortlist.com/980/64e26285901427ffa2929b28af42758f:9e257bd3a63b26594616996f874d3e3f/bruce-springsteen-darkness-on-the-edge-of-town",
    };
    private static int mCurrentCover = 0;

    private static final int MAX_RECENT_SONGS_SIZE = 10;
    private static final LinkedList<Song> mRecentSongs = new LinkedList<>();


    public static String getNextCover() {
        mCurrentCover = mCurrentCover % mCovers.length;
        return mCovers[mCurrentCover++];
    }

    public static void addToRecent(Song song) {
        if(mRecentSongs.contains(song)) {
            mRecentSongs.remove(song);
        }
        mRecentSongs.addFirst(song);

        if(mRecentSongs.size() > MAX_RECENT_SONGS_SIZE) {
            mRecentSongs.removeLast();
        }
    }

    public static List<Song> getRecent() {
        return mRecentSongs;
    }

    static public class SongInfo {
        public String artist;
        public String album;
        public String title;
        public int duration;
    }

    private static SongInfo getEmptySongInfo() {
        SongInfo emptyInfo = new SongInfo();
        emptyInfo.artist = "Unknown";
        emptyInfo.album = "Unknown";
        emptyInfo.title = "Unknown";
        emptyInfo.duration = 0;
        return emptyInfo;
    }

    public static SongInfo extractSongInfo(String songPath) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(songPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return getEmptySongInfo();
        }
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        try {
            metaRetriever.setDataSource(inputStream.getFD());
        } catch (Exception e) {
            e.printStackTrace();
            return getEmptySongInfo();
        }
        SongInfo songInfo = new SongInfo();
        songInfo.artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        songInfo.album = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        songInfo.title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        songInfo.duration = Integer.parseInt(duration);
        return songInfo;
    }

}
