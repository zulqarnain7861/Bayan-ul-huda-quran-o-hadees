/**
 * Bayan al-Huda — Server-Side Hadith API Proxy Worker
 *
 * Holds private API keys (`HADITH_API_KEY`, `SUNNAH_API_KEY`, `UMMAH_API_KEY`) strictly
 * in server-side secret storage so they are never embedded in client APK URLs or headers.
 *
 * Routes:
 * - GET /v1/hadithapi/books -> https://hadithapi.com/api/books?apiKey=<SERVER_SECRET>
 * - GET /v1/hadithapi/:slug/chapters -> https://hadithapi.com/api/:slug/chapters?apiKey=<SERVER_SECRET>
 * - GET /v1/hadithapi/hadiths?book=... -> https://hadithapi.com/public/api/hadiths?apiKey=<SERVER_SECRET>&...
 * - GET /v1/sunnah/* -> https://api.sunnah.com/v1/* (with X-API-Key: <SERVER_SECRET>)
 * - GET /v1/ummah/* -> https://ummahapi.com/* (with Authorization: Bearer <SERVER_SECRET>)
 */

const ALLOWED_COLLECTIONS = new Set([
  'sahih-bukhari', 'sahih-muslim', 'abu-dawood', 'al-tirmidhi',
  'sunan-nasai', 'ibn-e-majah', 'mishkat', 'musnad-ahmad',
  'al-silsila-sahiha', 'bukhari', 'muslim', 'abudawud', 'tirmidhi',
  'nasai', 'ibnmajah', 'malik', 'ahmad', 'darimi', 'riyadussalihin',
  'adab', 'bulugh', 'shamail', 'nawawi40', 'qudsi40'
]);

export default {
  async fetch(request, env) {
    if (request.method !== 'GET') {
      return new Response(JSON.stringify({ error: 'Method Not Allowed' }), {
        status: 405,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    const url = new URL(request.url);
    const path = url.pathname;

    // 1. HadithAPI.com proxy route
    if (path.startsWith('/v1/hadithapi/')) {
      const apiKey = env.HADITH_API_KEY;
      if (!apiKey) {
        return new Response(JSON.stringify({ error: 'Server secret HADITH_API_KEY not configured' }), {
          status: 503,
          headers: { 'Content-Type': 'application/json' }
        });
      }

      const subPath = path.slice('/v1/hadithapi/'.length);
      let upstreamUrl;
      if (subPath === 'books') {
        upstreamUrl = new URL('https://hadithapi.com/api/books');
      } else if (subPath === 'hadiths') {
        const book = url.searchParams.get('book') || '';
        if (!ALLOWED_COLLECTIONS.has(book)) {
          return new Response(JSON.stringify({ error: 'Invalid book slug' }), { status: 400 });
        }
        upstreamUrl = new URL('https://hadithapi.com/public/api/hadiths');
        for (const [k, v] of url.searchParams.entries()) {
          if (k !== 'apiKey') upstreamUrl.searchParams.set(k, v);
        }
      } else if (subPath.endsWith('/chapters')) {
        const slug = subPath.split('/')[0];
        if (!ALLOWED_COLLECTIONS.has(slug)) {
          return new Response(JSON.stringify({ error: 'Invalid book slug' }), { status: 400 });
        }
        upstreamUrl = new URL(`https://hadithapi.com/api/${encodeURIComponent(slug)}/chapters`);
      } else {
        return new Response(JSON.stringify({ error: 'Unknown HadithAPI route' }), { status: 404 });
      }

      upstreamUrl.searchParams.set('apiKey', apiKey);
      const resp = await fetch(upstreamUrl.toString(), {
        headers: { 'Accept': 'application/json' }
      });
      return new Response(resp.body, {
        status: resp.status,
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Cache-Control': 'public, max-age=3600'
        }
      });
    }

    // 2. Sunnah.com proxy route
    if (path.startsWith('/v1/sunnah/')) {
      const apiKey = env.SUNNAH_API_KEY;
      if (!apiKey) {
        return new Response(JSON.stringify({ error: 'Server secret SUNNAH_API_KEY not configured' }), {
          status: 503,
          headers: { 'Content-Type': 'application/json' }
        });
      }
      const subPath = path.slice('/v1/sunnah/'.length);
      const upstreamUrl = new URL(`https://api.sunnah.com/v1/${subPath}`);
      upstreamUrl.search = url.search;
      const resp = await fetch(upstreamUrl.toString(), {
        headers: {
          'Accept': 'application/json',
          'X-API-Key': apiKey
        }
      });
      return new Response(resp.body, {
        status: resp.status,
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Cache-Control': 'public, max-age=3600'
        }
      });
    }

    // 3. UmmahAPI proxy route
    if (path.startsWith('/v1/ummah/')) {
      const apiKey = env.UMMAH_API_KEY;
      const subPath = path.slice('/v1/ummah/'.length);
      const upstreamUrl = new URL(`https://ummahapi.com/${subPath}`);
      upstreamUrl.search = url.search;
      const headers = { 'Accept': 'application/json' };
      if (apiKey) {
        headers['Authorization'] = `Bearer ${apiKey}`;
        headers['X-API-Key'] = apiKey;
      }
      const resp = await fetch(upstreamUrl.toString(), { headers });
      return new Response(resp.body, {
        status: resp.status,
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Cache-Control': 'public, max-age=3600'
        }
      });
    }

    return new Response(JSON.stringify({ status: 'ok', service: 'bayan-al-huda-hadith-proxy' }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  }
};
