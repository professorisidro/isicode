# Especialização React + TypeScript + Vite

A stack padrão é:

- React;
- TypeScript;
- Vite;
- componentes funcionais;
- CSS convencional ou CSS Modules;
- `fetch` para chamadas HTTP.

Não use outro framework ou biblioteca visual sem solicitação explícita ou necessidade real.

# Criação de projeto

Quando o workspace estiver vazio, você pode criar a estrutura mínima manualmente ou executar:

`npm create vite@latest . -- --template react-ts`

Depois instale as dependências com `npm install`.

Antes de criar um projeto, confirme que não há um `package.json` que deva ser preservado.

# Estrutura recomendada

Use somente as pastas necessárias:

- `src/components`
- `src/pages`
- `src/services`
- `src/hooks`
- `src/types`
- `src/assets`

Mantenha `App.tsx` e `main.tsx` simples.

# React

- Use componentes funcionais.
- Mantenha cada componente com responsabilidade clara.
- Extraia componentes quando houver reutilização real.
- Não crie abstrações antecipadas.
- Evite estado duplicado.
- Use hooks apenas quando forem necessários.
- Não use `useEffect` para cálculos que podem acontecer durante a renderização.

# TypeScript

- Não use `any`.
- Declare tipos para propriedades, respostas de API e estados relevantes.
- Remova imports e variáveis não utilizados.
- Não use comentários para esconder erros de tipagem.
- Não desative regras do TypeScript sem justificativa.

# Interface

- Crie interfaces responsivas.
- Use HTML semântico.
- Formulários precisam de `label` associado aos campos.
- Botões devem indicar claramente a ação.
- Imagens precisam de texto alternativo.
- Preserve contraste e navegação por teclado.
- Não use Lorem Ipsum quando o contexto do usuário permitir conteúdo realista.

# Estilos

- Prefira CSS simples e organizado.
- Evite estilos inline, exceto valores realmente dinâmicos.
- Não instale Tailwind, Bootstrap, Material UI ou bibliotecas semelhantes sem solicitação.
- Garanta comportamento adequado em telas móveis.

# APIs e formulários

- Coloque integrações HTTP em `src/services` quando houver complexidade suficiente.
- Use `fetch`.
- Trate estados de carregamento, sucesso e erro.
- Valide campos obrigatórios antes do envio.
- Não exponha segredos ou chaves privadas no frontend.

# Validação

Após alterações de código:

1. execute `npm run build`;
2. corrija erros de TypeScript, imports e sintaxe;
3. repita o build até obter código de saída zero;
4. não inicie `npm run dev`, pois o usuário fará isso em outro terminal.
