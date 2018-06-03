package com.nutrition.express.application;

/**
 * Created by huang on 5/16/16.
 */
public class Constants {
    public static final String BASE_URL = "https://api.tumblr.com";
    public static final String CONSUMER_KEY = "FBRAQokq2dx5ups85qzQiPUvB7IbAPlUlcS3R0Ueok4E0gUFUj";
    public static final String CONSUMER_SECRET = "7pYdY24t6AZMBxK4TIIZH7200PUDFy3ickgLMt98otgqcuz1E7";
    public static final String REDIRECT_URI = "https://hm279atgae4.firebaseapp.com/__/auth/handler";

    public static final String REQUEST_TOKEN_URL = "https://www.tumblr.com/oauth/request_token";
    public static final String AUTHORIZE_URL = "https://www.tumblr.com/oauth/authorize";
    public static final String ACCESS_TOKEN_URL = "https://www.tumblr.com/oauth/access_token";

    public static final String API_KEYS ="{" +
            "\"w1sv2qNQIjyybAJIQcPW1OaDgZsvcoxbayNMQ0c5wp6ANMpLN9\":\"FDn86BsFVXg5gY11gp7bJ9sk11MOsHzDDUA4wGyozMj2bKqujR\"," +
            "\"NnkgFn7PBCw44hc60R8W1HjxtgDOQJHr5UCe9ShFWvDLDyYXql\":\"ttCyGLTvUgH34RQmM2jWBBnY9yAMa7l3NMsNBQ5OVamOSU5mlB\"," +
            "\"wlppcMogs82M4XyDVUMFpStYngCJLnNUZr8Tgl9AQEp2z7LGhS\":\"jGok87W55H0qOQgbCobcJ6ngNiQyO8MyE5O6JSzGc1qDetPFoR\"," +
            "\"OEQmxERzfBSE5OnOz6FsaXHycTtoQnCwCzMjkjRVNyMkVPQn3W\":\"3jfCKS40dn2Md29voafNauDuk7STH0qgojyzmuGa2T3rHpsg3Z\"," +
            "\"8Ufq75XEXq7nOkLDIca6XoIAlMifJLXlXwaAnOJEqzm1TL515L\":\"XyXrIAv9iWeviViyROeGvc8urE4yrc52BnprLeMwdLpv6QouH5\"," +
            "\"FBRAQokq2dx5ups85qzQiPUvB7IbAPlUlcS3R0Ueok4E0gUFUj\":\"7pYdY24t6AZMBxK4TIIZH7200PUDFy3ickgLMt98otgqcuz1E7\"," +
            "\"iExXiIUfJ49u99qO50BGGM4CIAX6beJufjWcJGAG27Or81blYE\":\"VZNoLwCgh9BnygG2W7pTrNfmCZYaO0sJqCT8NMKKGqNzX5KXba\"," +
            "\"DZXJtfVCoS1VOojMBaB0FcE8NZ8urRbyZbZJTON8B9UWNqOAQn\":\"DtU7D12Tf1pm075bZ1MLB61JkIvjnaSz4zu4yD9g84osjoGxpd\"," +
            "\"PEEmiBw5zAg00QhDX0t3NVPfhEYPO8dFpEAHtAmBgamc30KSoK\":\"qESpBWkXiGxZlbN4aovJUVreMBoVyI28IGGXXaKOPkFmrReP1n\"," +
            "\"IlVPDT9QzuKgAqrllDAOXJYFfgkCQg6sN1kiBriFk8lZkZxxeO\":\"GSqHifeYUG9mQ6ImGirk9FcQPJY5pa9N5Gcealbq4XGHACw9kq\"," +
            "\"Tu74EOIHWfYaevVQiAOUNLMez89F6OPouWFJdNRX0cIjTXP7Nh\":\"SolvA3zyJWFFUuZ69jDbwq0sEIPLrf8msHPcHseWPMx5JwXiP7\"," +
            "\"iWdYJmZ39Jkb0Sw2QXrA2nEmM9ilMkjrCRWwITV2IFxlolhl3c\":\"wzpxL8UMFjLMHIhlBbFBZPVAPmNQKax6Nk7qai3AQe5UPUYaUA\"," +
            "\"ZAk03jCf06dqblDmsZw0Bo8PBcCzdZz5nJybhTNhrnwc96KbOM\":\"oPeY3l2CkDMkcdHr1KZybuQVHgmUmFEA3DrbMppOZrNVUiHyeH\"" +
            "\"Hs2Q1Dd78HPsaxTlNhAaqsGL1IuuCzeztTyGzLcs4FhmTNZJry\":\"VbVGaZl1ynLgsqcA5BotQO1uvCLzu17bBQBAhQvCVS0wvtBHI1\"" +
            "\"N0su25jhIMDrksrXFRsml7txWSWu6t7PdZTzzjn3WqvHpoMAyX\":\"qfPkQrLrgnW1DUvEctEIF2hsmtu1I5AbPwjyL5l376ecl1jZLA\"" +
            "\"1OPyE0it5PczrRINdzYM83Vo96pXLRmEsVwReKWZJHcCNJxAvC\":\"sACkxK1yhzI4WInS8C7Cee3G00ISYniFOEXEZcctGV8vIeDhHU\"" +
            "\"WEclmvaqUrOvAVJo5LQFClFzY8oTWTgyUVFrYy5SzJCOS9QOC8\":\"9kW0zZjVoioIdO9biQpXGlPytZMk4qVl4IMt4FnkCv2nb5djXt\"" +
            "\"aISIRjbce6qSFRdtNO6NAvk1IGEhXYStahxcgTiP25O3fpkAyw\":\"4sHRQzmjlJEql0AuIVoqh8A0Ewu8LHL2L60uk6Hd973hGfCju4\"" +
            "\"CUO2d7QEhkTr9oisakitLbIfB2lWS0fnHbBRZe05ahqSqqLK69\":\"2H8nngcYpsoJtKucZgi75vIOzMjzCKCxxEGJNJ09vKXwkevucI\"" +
            "}";
}
