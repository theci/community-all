FROM node:20-alpine AS base
RUN npm install -g pnpm

FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app

# Copy workspace configuration
COPY pnpm-workspace.yaml package.json pnpm-lock.yaml ./

# Copy all package.json files for workspace packages
COPY packages/design-system/package.json ./packages/design-system/
COPY packages/api-contract/package.json ./packages/api-contract/
COPY packages/types/package.json ./packages/types/
COPY packages/native-bridge/package.json ./packages/native-bridge/
COPY packages/shared-hooks/package.json ./packages/shared-hooks/
COPY packages/mobile-ui/package.json ./packages/mobile-ui/
COPY apps/mobile-app/package.json ./apps/mobile-app/

# Install dependencies
RUN pnpm install --frozen-lockfile

FROM base AS builder
WORKDIR /app

# Copy node_modules from deps stage
COPY --from=deps /app/node_modules ./node_modules

# Copy workspace packages source code
COPY packages ./packages

# Copy mobile-app source code
COPY apps/mobile-app ./apps/mobile-app

# Copy workspace configuration
COPY pnpm-workspace.yaml package.json ./

ARG NEXT_PUBLIC_API_URL=http://3.39.36.234:8080/api/v1
ENV NEXT_TELEMETRY_DISABLED=1
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}

WORKDIR /app/apps/mobile-app
RUN pnpm build

FROM base AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder --chown=nextjs:nodejs /app ./

USER nextjs
WORKDIR /app/apps/mobile-app
EXPOSE 3001
ENV PORT=3001
ENV HOSTNAME="0.0.0.0"
CMD ["npx", "next", "start"]
