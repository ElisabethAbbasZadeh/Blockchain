import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.*;
import java.util.List;

public class Politicien extends Client {
    Block block;
    private List<String> word_pool;
    private KeyPair _key;
    private String publicKeyHexa;

    public Politicien(String serverHost, int port) throws JSONException, UnknownHostException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        super(serverHost, port);

        ED25519 ed = new ED25519();
        _key  = ed.genKeys();
        publicKeyHexa = Util.bytesToHex(((EdDSAPublicKey) _key.getPublic()).getAbyte());
    }

    @Override
    protected boolean traitementMessage(String msg) throws JSONException {
        if (super.traitementMessage(msg))
            return true;
        else if(Messages.isFullWordPool(msg)){
            word_pool=Messages.fullWordPool(msg);
            return true;
        }
        else if(Messages.isDiffWordPool(msg)){
            for(String w : Messages.diffWordPool(msg))
                if(!word_pool.contains(w))
                    word_pool.add(w);
            return true;
        }
        return false;
    }

    public void injectWord(List<String> lettres) throws JSONException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, IOException {
        JSONObject word = new JSONObject();
        word.put("word", Util.listToJSONArray(lettres));
        word.put("head", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        word.put("politician", publicKeyHexa);
        word.put("signature", Util.bytesToHex(Sha.signWord(_key, lettres, 0, Sha.hash_sha256(""))));
        JSONObject inject_word = new JSONObject();
        inject_word.put("inject_word", word);
        Util.writeMsg(os, inject_word);
    }
}
