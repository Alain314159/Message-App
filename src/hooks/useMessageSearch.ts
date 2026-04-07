import { useState, useCallback } from 'react';
import { supabase } from '@/services/supabase/config';
import type { Message } from '@/types';

export function useMessageSearch(chatId: string) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Message[]>([]);
  const [searching, setSearching] = useState(false);

  const search = useCallback(
    async (searchQuery: string) => {
      if (!searchQuery.trim()) {
        setResults([]);
        return;
      }
      setSearching(true);
      setQuery(searchQuery);
      try {
        const { data, error } = await supabase
          .from('messages')
          .select('*')
          .eq('chat_id', chatId)
          .ilike('content', `%${searchQuery}%`)
          .order('created_at', { ascending: false })
          .limit(50);
        if (error) {
          console.error('Search error:', error);
          setResults([]);
        } else {
          setResults(data as unknown as Message[]);
        }
      } finally {
        setSearching(false);
      }
    },
    [chatId],
  );

  const clear = useCallback(() => {
    setQuery('');
    setResults([]);
  }, []);

  return { query, results, searching, search, clear };
}
