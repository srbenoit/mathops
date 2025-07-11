package dev.mathops.web.host.course.lti;

import dev.mathops.commons.log.Log;
import dev.mathops.text.internet.RFC7518;
import dev.mathops.text.internet.RFC8017;
import dev.mathops.text.internet.RFC8017PublicKey;
import dev.mathops.text.parser.json.JSONObject;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A JSON Web Key Set.
 */
public final class JWKS {

    /** The datetime this set will expire, after which it should be reloaded from the LTI server. */
    public final LocalDateTime expiration;

    /** The keys in this key set. */
    private final List<JWK> keys;

    /**
     * Constructs a new {@code JWKS} from a JSON object parsed from the data loaded from the JWKS endpoint of the LTI
     * platform.
     *
     * @param json          the JSON object to parse
     * @param theExpiration the date/time this object will expire
     */
    public JWKS(final JSONObject json, final LocalDateTime theExpiration) {

        this.expiration = theExpiration;

        final Object fileKeys = json.getProperty("keys");
        if (fileKeys instanceof Object[] keysArray) {
            this.keys = new ArrayList<>(keysArray.length);

            for (final Object key : keysArray) {
                if (key instanceof JSONObject jsonKey) {
                    final JWK jwk = new JWK(jsonKey);
                    this.keys.add(jwk);
                } else {
                    Log.warning("Data from JWKS endpoint had unexpected item in 'keys' array.");
                }
            }
        } else {
            Log.warning("Data from JWKS endpoint did not have 'keys' array.");
            this.keys = new ArrayList<>(0);
        }
    }

    /**
     * Gets the number of keys.
     *
     * @return the number of keys
     */
    public int getNumKeys() {

        return this.keys.size();
    }

    /**
     * Gets the key at a specified index.
     *
     * @param index the index
     * @return the key
     */
    public JWK getKey(final int index) {

        return this.keys.get(index);
    }

    /**
     * A single JSON Web Key.
     */
    public final class JWK {

        /** The key ID. */
        public final String kid;

        /** The key type. */
        public final String kty;

        /** The key Exponent. */
        public final String e;

        /** The key modulus. */
        public final String n;

        /** The key algorithm. */
        public final String alg;

        /** The key use. */
        public final String use;

        /** The public key object. */
        public final RFC8017PublicKey publicKey;

        /**
         * Constructs a new {@code JWK}.
         *
         * @param json the JSON object to parse
         */
        JWK(final JSONObject json) {

            this.kid = json.getStringProperty("kid");
            this.kty = json.getStringProperty("kty");
            this.e = json.getStringProperty("e");
            this.n = json.getStringProperty("n");
            this.alg = json.getStringProperty("alg");
            this.use = json.getStringProperty("use");

            RFC8017PublicKey pub = null;

            if ("RSA".equals(this.kty)) {
                if (this.n == null || this.e == null) {
                    Log.warning("Required parameters missing - unable to create public key.");
                } else {
                    final Base64.Decoder decoder = Base64.getUrlDecoder();
                    try {
                        final byte[] nBytes = decoder.decode(this.n);
                        final BigInteger bigN = RFC8017.OS2IP(nBytes);

                        final byte[] eBytes = decoder.decode(this.e);
                        final BigInteger bigE = RFC8017.OS2IP(eBytes);

                        pub = new RFC8017PublicKey(bigN, bigE);
                    } catch (final IllegalArgumentException ex) {
                        Log.warning("Unable to parse 'n' and 'e' to create public key.", ex);
                    }
                }
            } else {
                Log.warning("Unsupported keyt type found: ", this.kty);
            }

            this.publicKey = pub;
        }
    }
}
