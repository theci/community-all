FROM node:20-alpine AS base
RUN npm install -g pnpm

FROM base AS deps
RUN apk add --no-cache libc6-compat
WORKDIR /app
COPY pnpm-workspace.yaml package.json pnpm-lock.yaml ./
COPY apps/admin/package.json ./apps/admin/
RUN pnpm install --frozen-lockfile --shamefully-hoist

FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY apps/admin ./apps/admin
COPY pnpm-workspace.yaml package.json ./

ARG NEXT_PUBLIC_API_URL=http://3.39.36.234:8080/api/v1
ENV NEXT_TELEMETRY_DISABLED=1
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}

WORKDIR /app/apps/admin
RUN pnpm build

FROM base AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Copy the entire standalone output
COPY --from=builder --chown=nextjs:nodejs /app/apps/admin/.next/standalone ./
# Copy the full node_modules from builder to resolve symlink issues
COPY --from=builder --chown=nextjs:nodejs /app/node_modules ./node_modules
# Copy static files to the correct location for standalone
COPY --from=builder --chown=nextjs:nodejs /app/apps/admin/.next/static ./.next/static
COPY --from=builder --chown=nextjs:nodejs /app/apps/admin/public ./public

USER nextjs
EXPOSE 3002
ENV PORT=3002
ENV HOSTNAME="0.0.0.0"
CMD ["node", "server.js"]
